package co.edu.unbosque.marketplace.usuario.application;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.Stream;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.usuario.application.port.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.application.port.VerificacionCorreoRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.domain.VerificacionCorreo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionCorreoServiceTest {

    private static final String CORREO = "ana@ejemplo.com";
    private static final String ULTIMO = "verificacion:correo:ultimo-envio:" + CORREO;
    private static final String CONTADOR = "verificacion:correo:contador-reenvios:" + CORREO;
    private static final String CODIGO = "123456";
    private static final String HASH =
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

    @Mock private VerificacionCorreoRepository verificacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AlmacenTemporal almacenTemporal;
    @Mock private CorreoOutboxService correoOutboxService;
    @InjectMocks private VerificacionCorreoService service;

    private Usuario encontrarUsuario() {
        Usuario u = new Usuario();
        u.setCorreo(CORREO);
        u.setCorreoVerificado(false);
        u.setEstado(EstadoUsuario.PENDIENTE_VERIFICACION);
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(u));
        return u;
    }

    private void usarRedis() {    }

    private VerificacionCorreo codigoPendiente(Usuario u) {
        VerificacionCorreo v = new VerificacionCorreo();
        v.setUsuario(u);
        v.setCodigoHash(HASH);
        v.setFechaExpiracion(OffsetDateTime.now().plusMinutes(10));
        v.setIntentos(0);
        v.setVerificado(false);
        when(verificacionRepository
                .findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(u))
                .thenReturn(Optional.of(v));
        return v;
    }

    private void permitirReenvio(long numero) {
        usarRedis();
        when(almacenTemporal.guardarSiAusente(ULTIMO, "1", Duration.ofSeconds(60L))).thenReturn(true);
        when(almacenTemporal.incrementar(CONTADOR)).thenReturn(numero);
    }

    private void verificarSinEnvio() {
        verifyNoInteractions(verificacionRepository, correoOutboxService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void rechazaCorreoInvalido(String correo) {
        assertAll(
                () -> assertEquals("Debes proporcionar un correo electrónico.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.enviarCodigo(correo)).getMessage()),
                () -> assertEquals("Debes proporcionar un correo electrónico.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.reenviarCodigo(correo)).getMessage()),
                () -> assertEquals("Debes proporcionar un correo electrónico.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.confirmarCodigo(correo, CODIGO)).getMessage()));
        verifyNoInteractions(usuarioRepository, almacenTemporal, verificacionRepository);
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.empty());
        assertAll(
                () -> assertEquals("Usuario no encontrado.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.enviarCodigo(CORREO)).getMessage()),
                () -> assertEquals("Usuario no encontrado.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.reenviarCodigo(CORREO)).getMessage()),
                () -> assertEquals("Usuario no encontrado.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.confirmarCodigo(CORREO, CODIGO)).getMessage()));
        verificarSinEnvio();
        verifyNoInteractions(almacenTemporal);
    }

    @Test
    void rechazaCorreoYaVerificado() {
        Usuario u = encontrarUsuario();
        u.setCorreoVerificado(true);
        assertAll(
                () -> assertEquals("El correo ya está verificado.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.enviarCodigo(CORREO)).getMessage()),
                () -> assertEquals("El correo ya está verificado.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.reenviarCodigo(CORREO)).getMessage()),
                () -> assertEquals("El correo ya está verificado.",
                        assertThrows(DatosInvalidosException.class,
                                () -> service.confirmarCodigo(CORREO, CODIGO)).getMessage()));
        verificarSinEnvio();
        verifyNoInteractions(almacenTemporal);
    }

    // Envío inicial: código de seis dígitos, hash y vigencia de diez minutos.

    @Test
    void enviarCodigoInvalidaAnterioresGuardaHashYEnviaOriginal() throws Exception {
        Usuario u = encontrarUsuario();
        usarRedis();

        service.enviarCodigo("  " + CORREO + "  ");

        var guardado = ArgumentCaptor.forClass(VerificacionCorreo.class);
        var fecha = ArgumentCaptor.forClass(OffsetDateTime.class);
        var codigo = ArgumentCaptor.forClass(String.class);
        var orden = inOrder(verificacionRepository, correoOutboxService);
        orden.verify(verificacionRepository).invalidarCodigosPendientes(eq(u), fecha.capture());
        orden.verify(verificacionRepository).save(guardado.capture());
        orden.verify(correoOutboxService).encolarCodigoVerificacion(eq(CORREO), codigo.capture());

        String hashEsperado = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(codigo.getValue().getBytes(StandardCharsets.UTF_8)));
        VerificacionCorreo v = guardado.getValue();
        assertAll(
                () -> assertTrue(codigo.getValue().matches("\\d{6}")),
                () -> assertEquals(hashEsperado, v.getCodigoHash()),
                () -> assertSame(u, v.getUsuario()),
                () -> assertEquals(fecha.getValue().plusMinutes(10), v.getFechaExpiracion()),
                () -> assertFalse(v.isVerificado()),
                () -> assertEquals(0, v.getIntentos()));
        verify(almacenTemporal).guardar(ULTIMO, "1", Duration.ofSeconds(60L));
        verify(almacenTemporal, never()).incrementar(anyString());
    }

    @Test
    void enviarCodigoNormalizaClaveRedis() {
        Usuario u = new Usuario();
        u.setCorreo(CORREO);
        u.setCorreoVerificado(false);
        // El repositorio recibe el correo recortado; Redis usa minúsculas.
        when(usuarioRepository.findByCorreoIgnoreCase("ANA@EJEMPLO.COM")).thenReturn(Optional.of(u));
        usarRedis();

        service.enviarCodigo("  ANA@EJEMPLO.COM  ");

        verify(almacenTemporal).guardar(ULTIMO, "1", Duration.ofSeconds(60L));
        verify(correoOutboxService).encolarCodigoVerificacion(eq(CORREO), anyString());
    }

    @Test
    void enviarCodigoLiberaEsperaSiFallaElEncolado() {
        encontrarUsuario();
        usarRedis();
        RuntimeException fallo = new RuntimeException("Error de correo");
        doThrow(fallo).when(correoOutboxService).encolarCodigoVerificacion(eq(CORREO), anyString());

        assertSame(fallo, assertThrows(RuntimeException.class, () -> service.enviarCodigo(CORREO)));

        verify(almacenTemporal).eliminar(ULTIMO);
    }

    // Reenvíos: espera de 60 segundos y máximo tres por hora.

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3})
    void reenviarCodigoPermiteHastaTresReenvios(long numero) {
        encontrarUsuario();
        permitirReenvio(numero);

        service.reenviarCodigo(CORREO);

        verify(correoOutboxService).encolarCodigoVerificacion(eq(CORREO), anyString());
        verify(verificacionRepository).save(any(VerificacionCorreo.class));
        if (numero == 1) {
            verify(almacenTemporal).asignarVigencia(CONTADOR, Duration.ofMinutes(60L));
        } else {
            verify(almacenTemporal, never()).asignarVigencia(anyString(), any(Duration.class));
        }
    }

    static Stream<Arguments> esperasPendientes() {
        return Stream.of(
                Arguments.of(false, 25L, 25L),
                Arguments.of(false, -1L, 60L),
                Arguments.of(false, null, 60L),
                Arguments.of(null, 10L, 10L));
    }

    @ParameterizedTest
    @MethodSource("esperasPendientes")
    void reenviarCodigoRechazaMientrasHayEspera(Boolean permiso, Long ttl, long esperado) {
        encontrarUsuario();
        usarRedis();
        when(almacenTemporal.guardarSiAusente(ULTIMO, "1", Duration.ofSeconds(60L)))
                .thenReturn(Boolean.TRUE.equals(permiso));
        when(almacenTemporal.segundosRestantes(ULTIMO)).thenReturn(ttl);

        var error = assertThrows(DatosInvalidosException.class, () -> service.reenviarCodigo(CORREO));

        assertEquals("Debes esperar " + esperado + " segundos antes de solicitar otro código.",
                error.getMessage());
        verify(almacenTemporal, never()).incrementar(anyString());
        verificarSinEnvio();
    }

    @Test
    void reenviarCodigoLiberaEsperaSiContadorEsNulo() {
        encontrarUsuario();
        usarRedis();
        when(almacenTemporal.guardarSiAusente(ULTIMO, "1", Duration.ofSeconds(60L))).thenReturn(true);
        when(almacenTemporal.incrementar(CONTADOR)).thenReturn(null);

        var error = assertThrows(DatosInvalidosException.class, () -> service.reenviarCodigo(CORREO));

        assertEquals("No se pudo verificar el límite de reenvíos.", error.getMessage());
        verify(almacenTemporal).eliminar(ULTIMO);
        verificarSinEnvio();
    }

    @Test
    void cuartoReenvioSeRechazaYDevuelveCupo() {
        encontrarUsuario();
        permitirReenvio(4);

        var error = assertThrows(DatosInvalidosException.class, () -> service.reenviarCodigo(CORREO));

        assertEquals("Has alcanzado el máximo de 3 reenvíos por hora.", error.getMessage());
        verify(almacenTemporal).decrementar(CONTADOR);
        verificarSinEnvio();
    }

    static Stream<Arguments> contadoresTrasFallo() {
        return Stream.of(Arguments.of((Long) null), Arguments.of(0L), Arguments.of(1L));
    }

    @ParameterizedTest
    @MethodSource("contadoresTrasFallo")
    void reenviarCodigoDevuelveCupoYLiberaEsperaSiFallaElEncolado(Long contadorActual) {
        encontrarUsuario();
        permitirReenvio(2);
        when(almacenTemporal.decrementar(CONTADOR)).thenReturn(contadorActual);
        RuntimeException fallo = new RuntimeException("Error de correo");
        doThrow(fallo).when(correoOutboxService).encolarCodigoVerificacion(eq(CORREO), anyString());

        assertSame(fallo, assertThrows(RuntimeException.class, () -> service.reenviarCodigo(CORREO)));

        verify(almacenTemporal).eliminar(ULTIMO);
        verify(almacenTemporal).decrementar(CONTADOR);
        if (contadorActual != null && contadorActual <= 0) {
            verify(almacenTemporal).eliminar(CONTADOR);
        } else {
            verify(almacenTemporal, never()).eliminar(CONTADOR);
        }
    }

    // Confirmación: formato, vencimiento, intentos y activación de la cuenta.

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "1234567", "abcdef", " 123456"})
    void confirmarCodigoRechazaFormatoInvalido(String codigo) {
        encontrarUsuario();

        var error = assertThrows(DatosInvalidosException.class,
                () -> service.confirmarCodigo(CORREO, codigo));

        assertEquals("El código debe contener exactamente 6 dígitos.", error.getMessage());
        verifyNoInteractions(verificacionRepository, almacenTemporal);
    }

    @Test
    void confirmarCodigoRechazaSiNoHayCodigoPendiente() {
        Usuario u = encontrarUsuario();
        when(verificacionRepository.findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(u))
                .thenReturn(Optional.empty());

        var error = assertThrows(DatosInvalidosException.class,
                () -> service.confirmarCodigo(CORREO, CODIGO));

        assertEquals("No hay un código pendiente de verificación.", error.getMessage());
        verify(verificacionRepository, never()).save(any(VerificacionCorreo.class));
    }

    @Test
    void confirmarCodigoRechazaCodigoExpiradoSinContarIntento() {
        Usuario u = encontrarUsuario();
        VerificacionCorreo v = codigoPendiente(u);
        v.setFechaExpiracion(OffsetDateTime.now().minusMinutes(1));

        var error = assertThrows(DatosInvalidosException.class,
                () -> service.confirmarCodigo(CORREO, CODIGO));

        assertEquals("El código ha expirado.", error.getMessage());
        assertEquals(0, v.getIntentos());
        verify(verificacionRepository, never()).save(any(VerificacionCorreo.class));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    void confirmarCodigoRechazaLimiteDeIntentos(int intentos) {
        Usuario u = encontrarUsuario();
        VerificacionCorreo v = codigoPendiente(u);
        v.setIntentos(intentos);

        var error = assertThrows(DatosInvalidosException.class,
                () -> service.confirmarCodigo(CORREO, CODIGO));

        assertEquals("Superaste el límite de intentos. Solicita otro código.", error.getMessage());
        assertEquals(intentos, v.getIntentos());
        verify(verificacionRepository, never()).save(any(VerificacionCorreo.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void confirmarCodigoIncorrectoGuardaIntentoSinActivarUsuario(int intentos) {
        Usuario u = encontrarUsuario();
        VerificacionCorreo v = codigoPendiente(u);
        v.setIntentos(intentos);

        // La excepción específica es privada; se comprueba mediante su tipo padre.
        var error = assertThrows(DatosInvalidosException.class,
                () -> service.confirmarCodigo(CORREO, "000000"));

        assertEquals("El código es incorrecto.", error.getMessage());
        assertEquals(intentos + 1, v.getIntentos());
        assertFalse(v.isVerificado());
        assertFalse(u.isCorreoVerificado());
        assertEquals(EstadoUsuario.PENDIENTE_VERIFICACION, u.getEstado());
        verify(verificacionRepository).save(v);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(almacenTemporal);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void confirmarCodigoCorrectoActivaCuentaYEliminaControles(int intentos) {
        Usuario u = encontrarUsuario();
        VerificacionCorreo v = codigoPendiente(u);
        v.setIntentos(intentos);
        OffsetDateTime antes = OffsetDateTime.now();

        service.confirmarCodigo("  " + CORREO + "  ", CODIGO);

        assertAll(
                () -> assertTrue(v.isVerificado()),
                () -> assertEquals(intentos + 1, v.getIntentos()),
                () -> assertTrue(u.isCorreoVerificado()),
                () -> assertEquals(EstadoUsuario.ACTIVA, u.getEstado()),
                () -> assertNotNull(u.getFechaActualizacion()),
                () -> assertFalse(u.getFechaActualizacion().isBefore(antes)),
                () -> assertFalse(u.getFechaActualizacion().isAfter(OffsetDateTime.now())));
        var orden = inOrder(verificacionRepository, usuarioRepository, almacenTemporal);
        orden.verify(verificacionRepository).save(v);
        orden.verify(usuarioRepository).save(u);
        orden.verify(almacenTemporal).eliminar(ULTIMO);
        orden.verify(almacenTemporal).eliminar(CONTADOR);
    }
}


