package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import co.edu.unbosque.marketplace.usuario.api.request.AutenticacionRequest;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.ProteccionContrasena;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    private static final String CORREO = "ana@ejemplo.com";
    private static final String CLAVE = "Clave123";
    private static final String HASH = "hash-prueba";

    private static final String MOTIVO_BLOQUEO =
            "Bloqueo automático por intentos fallidos";

    private static final OffsetDateTime FECHA_ANTERIOR =
            OffsetDateTime.parse("2020-01-01T00:00:00Z");

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProteccionContrasena proteccionContrasena;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AutenticacionService service;

    private static AutenticacionRequest solicitud(
            String correo,
            String contrasena) {

        AutenticacionRequest request = new AutenticacionRequest();
        request.setCorreo(correo);
        request.setContrasena(contrasena);
        return request;
    }

    private static Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setCorreo(CORREO);
        usuario.setContrasenaHash(HASH);
        usuario.setEstado(EstadoUsuario.ACTIVA);
        usuario.setCorreoVerificado(true);
        usuario.setRestringido(false);
        usuario.setIntentosFallidos(0);
        usuario.setFechaActualizacion(FECHA_ANTERIOR);
        usuario.setRol(null);
        return usuario;
    }

    private void configurarUsuarioEncontrado(Usuario usuario) {
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO))
            .thenReturn(Optional.of(usuario));
    }

    private void configurarClaveCorrecta() {
        when(proteccionContrasena.verificar(CLAVE, HASH))
            .thenReturn(true);
    }

    private void configurarRol(Usuario usuario, String nombre) {
        // Obtiene un mock del tipo de rol sin asumir su nombre de clase.
        Usuario referencia = mock(Usuario.class, RETURNS_DEEP_STUBS);
        var rol = referencia.getRol();

        when(rol.getNombre()).thenReturn(nombre);
        usuario.setRol(rol);
    }

    private void verificarFechaActualizada(
            Usuario usuario,
            OffsetDateTime antes,
            OffsetDateTime despues) {

        OffsetDateTime fecha = usuario.getFechaActualizacion();

        assertNotNull(fecha);
        assertFalse(fecha.isBefore(antes));
        assertFalse(fecha.isAfter(despues));
    }

    // VALIDACIÓN DE LA SOLICITUD

    static Stream<Arguments> solicitudesInvalidas() {
        return Stream.of(
            Arguments.of((AutenticacionRequest) null),
            Arguments.of(solicitud(null, CLAVE)),
            Arguments.of(solicitud("", CLAVE)),
            Arguments.of(solicitud("   ", CLAVE)),
            Arguments.of(solicitud(CORREO, null)),
            Arguments.of(solicitud(CORREO, "")),
            Arguments.of(solicitud(CORREO, "   "))
        );
    }

    @ParameterizedTest
    @MethodSource("solicitudesInvalidas")
    void rechazaSolicitudInvalida(AutenticacionRequest request) {
        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.iniciarSesion(request)
        );

        assertEquals(
            "Debes proporcionar el correo y la contraseña.",
            error.getMessage()
        );

        verifyNoInteractions(
            usuarioRepository, proteccionContrasena, jwtUtil
        );
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO))
            .thenReturn(Optional.empty());

        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.iniciarSesion(solicitud(CORREO, CLAVE))
        );

        assertEquals(
            "Correo o contraseña incorrectos.",
            error.getMessage()
        );

        verify(usuarioRepository).findByCorreoIgnoreCase(CORREO);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(proteccionContrasena, jwtUtil);
    }

    // CUENTAS NO HABILITADAS

    static Stream<Arguments> cuentasNoHabilitadas() {
        Usuario restringido = usuarioActivo();
        restringido.setRestringido(true);

        Usuario bloqueado = usuarioActivo();
        bloqueado.setIntentosFallidos(5);

        Usuario superaLimite = usuarioActivo();
        superaLimite.setIntentosFallidos(6);

        Usuario correoNoVerificado = usuarioActivo();
        correoNoVerificado.setCorreoVerificado(false);

        Usuario pendiente = usuarioActivo();
        pendiente.setEstado(EstadoUsuario.PENDIENTE_VERIFICACION);

        Usuario inactivo = usuarioActivo();
        inactivo.setEstado(EstadoUsuario.INACTIVA);

        Usuario sinEstado = usuarioActivo();
        sinEstado.setEstado(null);

        String mensajeBloqueo =
                "Tu cuenta está bloqueada por intentos fallidos. "
                + "Debes solicitar su reactivación a un Administrador.";

        return Stream.of(
            Arguments.of(
                restringido,
                "Tu cuenta tiene una restricción administrativa. "
                    + "No puedes iniciar sesión."
            ),
            Arguments.of(bloqueado, mensajeBloqueo),
            Arguments.of(superaLimite, mensajeBloqueo),
            Arguments.of(
                correoNoVerificado,
                "Debes verificar tu correo antes de iniciar sesión."
            ),
            Arguments.of(
                pendiente,
                "Debes verificar tu correo antes de iniciar sesión."
            ),
            Arguments.of(
                inactivo,
                "Tu cuenta está desactivada."
            ),
            Arguments.of(
                sinEstado,
                "La cuenta no se encuentra habilitada para iniciar sesión."
            )
        );
    }

    @ParameterizedTest
    @MethodSource("cuentasNoHabilitadas")
    void rechazaCuentaNoHabilitada(
            Usuario usuario,
            String mensajeEsperado) {

        configurarUsuarioEncontrado(usuario);

        var error = assertThrows(
            IllegalStateException.class,
            () -> service.iniciarSesion(solicitud(CORREO, CLAVE))
        );

        assertEquals(mensajeEsperado, error.getMessage());

        verify(usuarioRepository).findByCorreoIgnoreCase(CORREO);
        verifyNoMoreInteractions(usuarioRepository);
        verifyNoInteractions(proteccionContrasena, jwtUtil);
    }

    // CONTRASEÑA INCORRECTA

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void claveIncorrectaIncrementaIntentosSinBloquear(int intentos) {
        Usuario usuario = usuarioActivo();
        usuario.setIntentosFallidos(intentos);
        usuario.setMotivoBloqueo(null);

        configurarUsuarioEncontrado(usuario);

        when(proteccionContrasena.verificar(CLAVE, HASH))
            .thenReturn(false);

        OffsetDateTime antes = OffsetDateTime.now();

        var error = assertThrows(
            IllegalArgumentException.class,
            () -> service.iniciarSesion(solicitud(CORREO, CLAVE))
        );

        OffsetDateTime despues = OffsetDateTime.now();

        assertAll(
            () -> assertEquals(
                "Correo o contraseña incorrectos.", error.getMessage()
            ),
            () -> assertEquals(
                intentos + 1, usuario.getIntentosFallidos()
            ),
            () -> assertNull(usuario.getMotivoBloqueo()),
            () -> verificarFechaActualizada(usuario, antes, despues)
        );

        verify(proteccionContrasena).verificar(CLAVE, HASH);
        verify(usuarioRepository).save(same(usuario));
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void quintoIntentoFallidoBloqueaLaCuenta() {
        Usuario usuario = usuarioActivo();
        usuario.setIntentosFallidos(4);

        configurarUsuarioEncontrado(usuario);

        when(proteccionContrasena.verificar(CLAVE, HASH))
            .thenReturn(false);

        OffsetDateTime antes = OffsetDateTime.now();

        var error = assertThrows(
            IllegalStateException.class,
            () -> service.iniciarSesion(solicitud(CORREO, CLAVE))
        );

        OffsetDateTime despues = OffsetDateTime.now();

        assertAll(
            () -> assertEquals(
                "Tu cuenta ha sido bloqueada por alcanzar "
                    + "cinco intentos fallidos. "
                    + "Debes solicitar su reactivación a un Administrador.",
                error.getMessage()
            ),
            () -> assertEquals(5, usuario.getIntentosFallidos()),
            () -> assertEquals(
                MOTIVO_BLOQUEO, usuario.getMotivoBloqueo()
            ),
            () -> verificarFechaActualizada(usuario, antes, despues)
        );

        verify(usuarioRepository).save(same(usuario));
        verifyNoInteractions(jwtUtil);
    }

    // VALIDACIÓN DEL ROL

    @Test
    void rechazaUsuarioSinRol() {
        Usuario usuario = usuarioActivo();
        configurarUsuarioEncontrado(usuario);
        configurarClaveCorrecta();

        var error = assertThrows(
            IllegalStateException.class,
            () -> service.iniciarSesion(solicitud(CORREO, CLAVE))
        );

        assertEquals(
            "La cuenta no tiene un rol asignado.",
            error.getMessage()
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtUtil);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaRolConNombreInvalido(String nombreRol) {
        Usuario usuario = usuarioActivo();
        configurarRol(usuario, nombreRol);
        configurarUsuarioEncontrado(usuario);
        configurarClaveCorrecta();

        var error = assertThrows(
            IllegalStateException.class,
            () -> service.iniciarSesion(solicitud(CORREO, CLAVE))
        );

        assertEquals(
            "La cuenta no tiene un rol asignado.",
            error.getMessage()
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(jwtUtil);
    }

    // INICIO DE SESIÓN EXITOSO

    @Test
    void loginExitosoRecortaCorreoYRetornaTokenSinGuardarUsuario() {
        Usuario usuario = usuarioActivo();
        configurarRol(usuario, "HUESPED");
        configurarUsuarioEncontrado(usuario);
        configurarClaveCorrecta();

        when(jwtUtil.generateToken(usuario)).thenReturn("token-prueba");

        String token = service.iniciarSesion(
            solicitud("  " + CORREO + "  ", CLAVE)
        );

        assertAll(
            () -> assertEquals("token-prueba", token),
            () -> assertEquals(0, usuario.getIntentosFallidos()),
            () -> assertEquals(
                FECHA_ANTERIOR, usuario.getFechaActualizacion()
            )
        );

        verify(usuarioRepository).findByCorreoIgnoreCase(CORREO);
        verify(proteccionContrasena).verificar(CLAVE, HASH);
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(jwtUtil).generateToken(same(usuario));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void loginExitosoReiniciaIntentosYLimpiaMotivo(int intentos) {
        Usuario usuario = usuarioActivo();
        usuario.setIntentosFallidos(intentos);
        usuario.setMotivoBloqueo("Motivo anterior");

        configurarRol(usuario, "ANFITRION");
        configurarUsuarioEncontrado(usuario);
        configurarClaveCorrecta();

        when(jwtUtil.generateToken(usuario)).thenReturn("token-prueba");

        OffsetDateTime antes = OffsetDateTime.now();

        String token = service.iniciarSesion(solicitud(CORREO, CLAVE));

        OffsetDateTime despues = OffsetDateTime.now();

        assertAll(
            () -> assertEquals("token-prueba", token),
            () -> assertEquals(0, usuario.getIntentosFallidos()),
            () -> assertNull(usuario.getMotivoBloqueo()),
            () -> verificarFechaActualizada(usuario, antes, despues)
        );

        var orden = inOrder(usuarioRepository, jwtUtil);
        orden.verify(usuarioRepository).findByCorreoIgnoreCase(CORREO);
        orden.verify(usuarioRepository).save(same(usuario));
        orden.verify(jwtUtil).generateToken(same(usuario));
    }
}
