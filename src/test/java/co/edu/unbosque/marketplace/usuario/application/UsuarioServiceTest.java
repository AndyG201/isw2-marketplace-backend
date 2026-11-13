package co.edu.unbosque.marketplace.usuario.application;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import co.edu.unbosque.marketplace.usuario.api.request.*;
import co.edu.unbosque.marketplace.usuario.domain.*;
import co.edu.unbosque.marketplace.usuario.infrastructure.email.ExternalHTTPRequestMapping;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.*;
import co.edu.unbosque.marketplace.usuario.security.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import co.edu.unbosque.marketplace.usuario.contracts.CuentaDesactivadaEvent;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String CORREO = "ana@ejemplo.com";
    private static final String PREFIJO = "perfil:cambio-correo:" + ID;
    private static final String CLAVE_CODIGO = "recuperacion:contrasena:" + CORREO;
    // SHA-256 del código 123456.
    private static final String HASH_CODIGO =
            "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private ProteccionDocumento proteccionDocumento;
    @Mock private ProteccionContrasena proteccionContrasena;
    @Mock private VerificacionCorreoService verificacionCorreoService;
    @Mock private ExternalHTTPRequestMapping correoExterno;
    @Mock private JwtUtil jwtUtil;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StringRedisTemplate redisTemplate;
    @InjectMocks private UsuarioService service;

    private Usuario usuario() {
        Usuario u = new Usuario();
        u.setIdUsuario(ID);
        u.setNombre("Ana");
        u.setApellido("Perez");
        u.setCorreo(CORREO);
        u.setContrasenaHash("hash-anterior");
        u.setEstado(EstadoUsuario.ACTIVA);
        u.setCorreoVerificado(true);
        return u;
    }

    private void encontrar(Usuario u) {
        when(usuarioRepository.findById(ID)).thenReturn(Optional.of(u));
    }

    private ActualizarContrasenaRequest contrasena() {
        ActualizarContrasenaRequest r = new ActualizarContrasenaRequest();
        r.setContrasenaActual("Actual123");
        r.setNuevaContrasena("Nueva456");
        return r;
    }

    @Test
    void registrarUsuarioValido() {
        RegistrarUsuarioRequest r = new RegistrarUsuarioRequest();
        r.setNombre("Ana");
        r.setApellido("Perez");
        r.setCorreo(CORREO);
        r.setTipoDocumento("CEDULA");
        r.setDocumentoIdentidad("123456789");
        r.setTelefono("3001234567");
        r.setContrasena("Clave123");
        r.setRol(" huesped ");

        Rol rol = mock(Rol.class);
        var datos = mock(ProteccionDocumento.DatosDocumento.class);
        byte[] hash = {1, 2, 3};
        when(rolRepository.findByNombreIgnoreCase("HUESPED")).thenReturn(Optional.of(rol));
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.empty());
        when(proteccionDocumento.protegerDocumento("CEDULA", "123456789")).thenReturn(datos);
        when(datos.documentoHash()).thenReturn(hash);
        when(proteccionContrasena.generarHash("Clave123")).thenReturn("hash-seguro");

        assertEquals(1, service.registrar(r));

        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertAll(
                () -> assertNotNull(guardado.getIdUsuario()),
                () -> assertEquals(CORREO, guardado.getCorreo()),
                () -> assertEquals("hash-seguro", guardado.getContrasenaHash()),
                () -> assertArrayEquals(hash, guardado.getDocumentoHash()),
                () -> assertSame(rol, guardado.getRol()),
                () -> assertFalse(guardado.isCorreoVerificado()),
                () -> assertEquals(EstadoUsuario.PENDIENTE_VERIFICACION, guardado.getEstado()));
        verify(verificacionCorreoService).enviarCodigo(CORREO);
    }

    @Test
    void registrarRechazaSolicitudNula() {
        assertEquals(0, service.registrar(null));
        verifyNoInteractions(usuarioRepository, rolRepository);
    }

    @Test
    void generarCodigoGuardaHashYEnviaCorreo() {
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(usuario()));

        assertTrue(service.generarCodigoRecuperacion("  " + CORREO + "  "));

        var codigo = ArgumentCaptor.forClass(String.class);
        verify(correoExterno).enviarCodigoRecuperacion(eq(CORREO), codigo.capture());
        assertTrue(codigo.getValue().matches("\\d{6}"));
        verify(redisTemplate.opsForValue()).set(
                eq(CLAVE_CODIGO), matches("[0-9a-f]{64}"), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void validarCodigoCorrecto() {
        when(redisTemplate.opsForValue().get(CLAVE_CODIGO)).thenReturn(HASH_CODIGO);
        assertTrue(service.validarCodigoRecuperacion("  ANA@EJEMPLO.COM  ", "123456"));
    }

    @Test
    void quintoCodigoIncorrectoEliminaRecuperacion() {
        String intentos = "recuperacion:contrasena:intentos:" + CORREO;
        when(redisTemplate.opsForValue().get(CLAVE_CODIGO)).thenReturn(HASH_CODIGO);
        when(redisTemplate.opsForValue().increment(intentos)).thenReturn(5L);

        assertFalse(service.validarCodigoRecuperacion(CORREO, "000000"));

        verify(redisTemplate).delete(CLAVE_CODIGO);
        verify(redisTemplate).delete(intentos);
    }

    @Test
    void restablecerContrasenaGuardaHashYEliminaCodigo() {
        Usuario u = usuario();
        when(redisTemplate.opsForValue().get(CLAVE_CODIGO)).thenReturn(HASH_CODIGO);
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(u));
        when(proteccionContrasena.generarHash("Nueva456")).thenReturn("hash-nuevo");

        assertTrue(service.restablecerContrasena(CORREO, "123456", "Nueva456"));

        assertEquals("hash-nuevo", u.getContrasenaHash());
        assertNotNull(u.getFechaActualizacion());
        verify(usuarioRepository).save(u);
        verify(redisTemplate).delete(CLAVE_CODIGO);
        verify(redisTemplate).delete("recuperacion:contrasena:intentos:" + CORREO);
    }

    @Test
    void consultarCuentaDescifraDocumento() {
        Usuario u = usuario();
        Rol rol = mock(Rol.class);
        when(rol.getNombre()).thenReturn("HUESPED");
        u.setRol(rol);
        u.setDocumentoCifrado(new byte[]{1});
        u.setDocumentoNonce(new byte[]{2});
        encontrar(u);
        when(proteccionDocumento.descifrarDocumento(
                u.getDocumentoCifrado(), u.getDocumentoNonce())).thenReturn("123456789");

        var respuesta = service.consultarMiCuenta(ID);

        assertAll(
                () -> assertEquals("Ana", respuesta.getNombre()),
                () -> assertEquals("Perez", respuesta.getApellido()),
                () -> assertEquals(CORREO, respuesta.getCorreo()),
                () -> assertEquals("123456789", respuesta.getDocumentoIdentidad()),
                () -> assertEquals("HUESPED", respuesta.getRol()));
    }

    @Test
    void actualizarPerfilSinCambiarCorreo() {
        Usuario u = usuario();
        encontrar(u);
        ActualizarPerfilRequest r = new ActualizarPerfilRequest();
        r.setNombre("  Carlos  ");
        r.setApellido("  Lopez  ");
        r.setCorreo(CORREO);

        assertEquals("Perfil actualizado correctamente.", service.actualizarPerfil(ID, r));

        assertEquals("Carlos", u.getNombre());
        assertEquals("Lopez", u.getApellido());
        verify(usuarioRepository).save(u);
        verifyNoInteractions(correoExterno, redisTemplate);
    }

    @Test
    void confirmarCambioActualizaCorreoYLimpiaSolicitud() {
        Usuario u = usuario();
        u.setCorreoVerificado(false);
        encontrar(u);
        when(redisTemplate.opsForValue().get(PREFIJO + ":correo")).thenReturn("nuevo@ejemplo.com");
        when(redisTemplate.opsForValue().get(PREFIJO + ":codigo")).thenReturn(HASH_CODIGO);

        service.confirmarCambioCorreo(ID, "123456");

        assertEquals("nuevo@ejemplo.com", u.getCorreo());
        assertTrue(u.isCorreoVerificado());
        verify(usuarioRepository).save(u);
        verify(redisTemplate).delete(PREFIJO + ":correo");
        verify(redisTemplate).delete(PREFIJO + ":codigo");
        verify(redisTemplate).delete(PREFIJO + ":intentos");
    }

    @Test
    void actualizarContrasenaGuardaNuevoHash() {
        Usuario u = usuario();
        encontrar(u);
        when(proteccionContrasena.verificar("Actual123", "hash-anterior")).thenReturn(true);
        when(proteccionContrasena.generarHash("Nueva456")).thenReturn("hash-nuevo");

        service.actualizarContrasena(ID, contrasena());

        assertEquals("hash-nuevo", u.getContrasenaHash());
        verify(usuarioRepository).save(u);
    }

    @Test
    void actualizarContrasenaRechazaClaveIncorrecta() {
        Usuario u = usuario();
        encontrar(u);
        var error = assertThrows(IllegalArgumentException.class,
                () -> service.actualizarContrasena(ID, contrasena()));

        assertEquals("La contraseña actual es incorrecta.", error.getMessage());
        assertEquals("hash-anterior", u.getContrasenaHash());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void desactivarCuentaInvalidaSesiones() {
        Usuario u = usuario();
        encontrar(u);

        service.desactivarCuenta(ID);

        assertEquals(EstadoUsuario.INACTIVA, u.getEstado());

        verify(usuarioRepository).save(u);

        verify(jwtUtil).invalidarSesiones(ID);

        verify(eventPublisher).publishEvent(
                any(CuentaDesactivadaEvent.class)
        );
    }

    @Test
    void desactivarCuentaRestringidaNoGuardaNiInvalidaSesiones() {
        Usuario u = usuario();
        u.setRestringido(true);
        encontrar(u);

        var error = assertThrows(IllegalStateException.class, () -> service.desactivarCuenta(ID));

        assertEquals("No puedes desactivar una cuenta con restricción administrativa.",
                error.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void reactivarCuentaGuardaEstadoActivo() {
        Usuario u = usuario();
        u.setEstado(EstadoUsuario.INACTIVA);
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(u));
        when(proteccionContrasena.verificar("Clave123", "hash-anterior")).thenReturn(true);
        ReactivarCuentaRequest r = new ReactivarCuentaRequest();
        r.setCorreo("  " + CORREO + "  ");
        r.setContrasena("Clave123");

        service.reactivarCuenta(r);

        assertEquals(EstadoUsuario.ACTIVA, u.getEstado());
        verify(usuarioRepository).save(u);
    }

    @ParameterizedTest
    @ValueSource(strings = {"correo", "telefono", "documento"})
    void registrarRechazaDatosDuplicados(String campo) {
        RegistrarUsuarioRequest r = new RegistrarUsuarioRequest();
        r.setNombre("Ana");
        r.setApellido("Perez");
        r.setCorreo(CORREO);
        r.setTipoDocumento("CEDULA");
        r.setDocumentoIdentidad("123456789");
        r.setTelefono("3001234567");
        r.setContrasena("Clave123");
        r.setRol("HUESPED");
        when(rolRepository.findByNombreIgnoreCase("HUESPED"))
                .thenReturn(Optional.of(mock(Rol.class)));
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO))
                .thenReturn(campo.equals("correo") ? Optional.of(usuario()) : Optional.empty());
        if (campo.equals("telefono")) {
            when(usuarioRepository.existsByTelefono("3001234567")).thenReturn(true);
        } else if (campo.equals("documento")) {
            var datos = mock(ProteccionDocumento.DatosDocumento.class);
            byte[] hash = {1, 2};
            when(proteccionDocumento.protegerDocumento("CEDULA", "123456789")).thenReturn(datos);
            when(datos.documentoHash()).thenReturn(hash);
            when(usuarioRepository.existsByDocumentoHash(hash)).thenReturn(true);
        }

        assertEquals(0, service.registrar(r));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(verificacionCorreoService, proteccionContrasena);
    }

    @Test
    void generarCodigoRetornaFalseSiNoExisteUsuario() {
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.empty());
        assertFalse(service.generarCodigoRecuperacion(CORREO));
        verifyNoInteractions(redisTemplate, correoExterno);
    }

    @Test
    void generarCodigoLimpiaRedisSiFallaElEnvio() {
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(usuario()));
        RuntimeException fallo = new RuntimeException("Error de correo");
        doThrow(fallo).when(correoExterno).enviarCodigoRecuperacion(eq(CORREO), anyString());

        assertSame(fallo, assertThrows(RuntimeException.class,
                () -> service.generarCodigoRecuperacion(CORREO)));

        verify(redisTemplate).delete(CLAVE_CODIGO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "12345", "1234567", "abcdef"})
    void validarCodigoRechazaFormatoInvalido(String codigo) {
        assertFalse(service.validarCodigoRecuperacion(CORREO, codigo));
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void restablecerNoModificaUsuarioCuandoCodigoHaExpirado() {
        when(redisTemplate.opsForValue().get(CLAVE_CODIGO)).thenReturn(null);

        assertFalse(service.restablecerContrasena(CORREO, "123456", "Nueva456"));

        verifyNoInteractions(usuarioRepository, proteccionContrasena);
    }

    @Test
    void consultarCuentaRechazaUsuarioInexistente() {
        when(usuarioRepository.findById(ID)).thenReturn(Optional.empty());
        var error = assertThrows(IllegalArgumentException.class,
                () -> service.consultarMiCuenta(ID));
        assertEquals("Usuario no encontrado.", error.getMessage());
        verifyNoInteractions(proteccionDocumento);
    }

    @Test
    void consultarCuentaRechazaUsuarioSinRol() {
        Usuario u = usuario();
        u.setRol(null);
        encontrar(u);
        var error = assertThrows(IllegalStateException.class,
                () -> service.consultarMiCuenta(ID));
        assertEquals("El usuario no tiene un rol asignado.", error.getMessage());
        verifyNoInteractions(proteccionDocumento);
    }

    private ActualizarPerfilRequest perfilConNuevoCorreo() {
        ActualizarPerfilRequest r = new ActualizarPerfilRequest();
        r.setNombre("Ana");
        r.setApellido("Perez");
        r.setCorreo("nuevo@ejemplo.com");
        return r;
    }

    @Test
    void actualizarPerfilDejaCambioDeCorreoPendienteDeConfirmacion() {
        Usuario u = usuario();
        encontrar(u);

        assertEquals("Perfil actualizado. Se envió un código al nuevo correo para confirmar el cambio.",
                service.actualizarPerfil(ID, perfilConNuevoCorreo()));

        assertEquals(CORREO, u.getCorreo());
        var codigo = ArgumentCaptor.forClass(String.class);
        verify(correoExterno).enviarCodigoVerificacion(eq("nuevo@ejemplo.com"), codigo.capture());
        assertTrue(codigo.getValue().matches("\\d{6}"));
        verify(redisTemplate.opsForValue()).set(
                PREFIJO + ":correo", "nuevo@ejemplo.com", 5L, TimeUnit.MINUTES);
        verify(redisTemplate.opsForValue()).set(
                eq(PREFIJO + ":codigo"), matches("[0-9a-f]{64}"), eq(5L), eq(TimeUnit.MINUTES));
        verify(redisTemplate.opsForValue()).set(PREFIJO + ":intentos", "0", 5L, TimeUnit.MINUTES);
        verify(usuarioRepository).save(u);
    }

    @Test
    void actualizarPerfilRechazaCorreoDeOtraCuenta() {
        encontrar(usuario());
        when(usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot("nuevo@ejemplo.com", ID))
                .thenReturn(true);
        var error = assertThrows(IllegalStateException.class,
                () -> service.actualizarPerfil(ID, perfilConNuevoCorreo()));
        assertEquals("El correo electrónico ya está asociado a otra cuenta.", error.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(redisTemplate, correoExterno);
    }

    private void verificarLimpiezaCambioCorreo() {
        verify(redisTemplate).delete(PREFIJO + ":correo");
        verify(redisTemplate).delete(PREFIJO + ":codigo");
        verify(redisTemplate).delete(PREFIJO + ":intentos");
    }

    @Test
    void actualizarPerfilLimpiaSolicitudSiFallaElEnvio() {
        encontrar(usuario());
        RuntimeException fallo = new RuntimeException("Error de correo");
        doThrow(fallo).when(correoExterno).enviarCodigoVerificacion(eq("nuevo@ejemplo.com"), anyString());

        var error = assertThrows(IllegalStateException.class,
                () -> service.actualizarPerfil(ID, perfilConNuevoCorreo()));

        assertEquals("No se pudo enviar el código al nuevo correo.", error.getMessage());
        assertSame(fallo, error.getCause());
        verificarLimpiezaCambioCorreo();
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void confirmarCambioRechazaSolicitudExpirada() {
        encontrar(usuario());
        var error = assertThrows(IllegalStateException.class,
                () -> service.confirmarCambioCorreo(ID, "123456"));
        assertEquals("El código ha expirado o no existe una solicitud pendiente.", error.getMessage());
        verificarLimpiezaCambioCorreo();
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 5})
    void confirmarCambioControlaCodigosIncorrectos(long intentos) {
        encontrar(usuario());
        when(redisTemplate.opsForValue().get(PREFIJO + ":correo")).thenReturn("nuevo@ejemplo.com");
        when(redisTemplate.opsForValue().get(PREFIJO + ":codigo")).thenReturn(HASH_CODIGO);
        when(redisTemplate.opsForValue().increment(PREFIJO + ":intentos")).thenReturn(intentos);

        if (intentos == 5) {
            var error = assertThrows(IllegalStateException.class,
                    () -> service.confirmarCambioCorreo(ID, "000000"));
            assertEquals("Superaste los cinco intentos. Solicita nuevamente el cambio de correo.",
                    error.getMessage());
            verificarLimpiezaCambioCorreo();
        } else {
            var error = assertThrows(IllegalArgumentException.class,
                    () -> service.confirmarCambioCorreo(ID, "000000"));
            assertEquals("El código de verificación es incorrecto.", error.getMessage());
            verify(redisTemplate, never()).delete(anyString());
        }
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void desactivarCuentaBloqueadaNoInvalidaSesiones() {
        Usuario u = usuario();
        u.setIntentosFallidos(5);
        encontrar(u);
        var error = assertThrows(IllegalStateException.class, () -> service.desactivarCuenta(ID));
        assertEquals("No puedes desactivar una cuenta bloqueada por intentos fallidos.", error.getMessage());
        assertEquals(EstadoUsuario.ACTIVA, u.getEstado());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void reactivarCuentaRechazaClaveIncorrecta() {
        Usuario u = usuario();
        u.setEstado(EstadoUsuario.INACTIVA);
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(u));
        ReactivarCuentaRequest r = new ReactivarCuentaRequest();
        r.setCorreo(CORREO);
        r.setContrasena("incorrecta");

        var error = assertThrows(IllegalArgumentException.class, () -> service.reactivarCuenta(r));

        assertEquals("Correo o contraseña incorrectos.", error.getMessage());
        assertEquals(EstadoUsuario.INACTIVA, u.getEstado());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}



