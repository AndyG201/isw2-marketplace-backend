package co.edu.unbosque.marketplace.usuario.api.response;

import java.util.UUID;
import java.util.stream.Stream;

import co.edu.unbosque.marketplace.usuario.api.CuentaController;
import co.edu.unbosque.marketplace.usuario.api.request.ActualizarContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.api.request.ActualizarPerfilRequest;
import co.edu.unbosque.marketplace.usuario.api.request.ConfirmarCambioCorreoRequest;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Usuario usuario;

    @InjectMocks
    private CuentaController controller;

    private final UUID idUsuario = UUID.randomUUID();

    private void configurarUsuario() {
        when(usuario.getIdUsuario()).thenReturn(idUsuario);
    }

    private ActualizarPerfilRequest solicitudPerfil() {
        ActualizarPerfilRequest solicitud = new ActualizarPerfilRequest();
        solicitud.setNombre("Ana");
        solicitud.setApellido("Perez");
        solicitud.setCorreo("ana@ejemplo.com");
        return solicitud;
    }

    private ConfirmarCambioCorreoRequest solicitudCorreo() {
        ConfirmarCambioCorreoRequest solicitud =
                new ConfirmarCambioCorreoRequest();
        solicitud.setCodigo("123456");
        return solicitud;
    }

    private ActualizarContrasenaRequest solicitudContrasena() {
        ActualizarContrasenaRequest solicitud =
                new ActualizarContrasenaRequest();
        solicitud.setContrasenaActual("ClaveActual123");
        solicitud.setNuevaContrasena("NuevaClave456");
        return solicitud;
    }

    static Stream<Arguments> erroresDelServicio() {
        return Stream.of(
            Arguments.of(
                new IllegalArgumentException("Datos inválidos."),
                HttpStatus.BAD_REQUEST
            ),
            Arguments.of(
                new IllegalStateException("Operación no permitida."),
                HttpStatus.CONFLICT
            )
        );
    }

    @Test
    void endpointsRetornan401CuandoUsuarioEsNulo() {
        assertAll(
            () -> verificarNoAutorizado(
                controller.consultarMiCuenta(null)
            ),
            () -> verificarNoAutorizado(
                controller.actualizarPerfil(null, solicitudPerfil())
            ),
            () -> verificarNoAutorizado(
                controller.confirmarCambioCorreo(null, solicitudCorreo())
            ),
            () -> verificarNoAutorizado(
                controller.actualizarContrasena(
                    null, solicitudContrasena()
                )
            ),
            () -> verificarNoAutorizado(
                controller.desactivarCuenta(null)
            )
        );

        verifyNoInteractions(usuarioService);
    }

    @Test
    void consultarMiCuentaRetornaOkConDatosDelServicio() {
        configurarUsuario();

        ConsultarCuentaResponse cuenta = new ConsultarCuentaResponse(
            "Ana",
            "Perez",
            "ana@ejemplo.com",
            "CEDULA",
            "123456789",
            "HUESPED"
        );

        when(usuarioService.consultarMiCuenta(idUsuario))
            .thenReturn(cuenta);

        var response = controller.consultarMiCuenta(usuario);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertSame(cuenta, response.getBody())
        );

        verify(usuarioService).consultarMiCuenta(idUsuario);
    }

    @Test
    void actualizarPerfilRetornaOkConResultadoDelServicio() {
        configurarUsuario();

        ActualizarPerfilRequest solicitud = solicitudPerfil();
        String resultado = "Perfil actualizado correctamente.";

        when(usuarioService.actualizarPerfil(idUsuario, solicitud))
            .thenReturn(resultado);

        var response = controller.actualizarPerfil(usuario, solicitud);

        verificarRespuesta(response, HttpStatus.OK, resultado);

        verify(usuarioService).actualizarPerfil(idUsuario, solicitud);
    }

    @ParameterizedTest
    @MethodSource("erroresDelServicio")
    void actualizarPerfilRetornaErrorDelServicio(
            RuntimeException error,
            HttpStatus estadoEsperado) {

        configurarUsuario();
        ActualizarPerfilRequest solicitud = solicitudPerfil();

        when(usuarioService.actualizarPerfil(idUsuario, solicitud))
            .thenThrow(error);

        var response = controller.actualizarPerfil(usuario, solicitud);

        verificarRespuesta(response, estadoEsperado, error.getMessage());

        verify(usuarioService).actualizarPerfil(idUsuario, solicitud);
    }

    @Test
    void confirmarCambioCorreoRetornaOk() {
        configurarUsuario();
        ConfirmarCambioCorreoRequest solicitud = solicitudCorreo();

        var response = controller.confirmarCambioCorreo(
            usuario, solicitud
        );

        verificarRespuesta(
            response,
            HttpStatus.OK,
            "El correo electrónico se actualizó y verificó correctamente."
        );

        verify(usuarioService).confirmarCambioCorreo(
            idUsuario, "123456"
        );
    }

    @ParameterizedTest
    @MethodSource("erroresDelServicio")
    void confirmarCambioCorreoRetornaErrorDelServicio(
            RuntimeException error,
            HttpStatus estadoEsperado) {

        configurarUsuario();
        ConfirmarCambioCorreoRequest solicitud = solicitudCorreo();

        doThrow(error)
            .when(usuarioService)
            .confirmarCambioCorreo(idUsuario, "123456");

        var response = controller.confirmarCambioCorreo(
            usuario, solicitud
        );

        verificarRespuesta(response, estadoEsperado, error.getMessage());

        verify(usuarioService).confirmarCambioCorreo(
            idUsuario, "123456"
        );
    }

    @Test
    void actualizarContrasenaRetornaOk() {
        configurarUsuario();
        ActualizarContrasenaRequest solicitud = solicitudContrasena();

        var response = controller.actualizarContrasena(
            usuario, solicitud
        );

        verificarRespuesta(
            response,
            HttpStatus.OK,
            "La contraseña se actualizó correctamente."
        );

        verify(usuarioService).actualizarContrasena(
            idUsuario, solicitud
        );
    }

    @Test
    void actualizarContrasenaRetorna400AnteIllegalArgumentException() {
        configurarUsuario();
        ActualizarContrasenaRequest solicitud = solicitudContrasena();
        String mensaje = "La contraseña actual es incorrecta.";

        doThrow(new IllegalArgumentException(mensaje))
            .when(usuarioService)
            .actualizarContrasena(idUsuario, solicitud);

        var response = controller.actualizarContrasena(
            usuario, solicitud
        );

        verificarRespuesta(response, HttpStatus.BAD_REQUEST, mensaje);

        verify(usuarioService).actualizarContrasena(
            idUsuario, solicitud
        );
    }

    @Test
    void desactivarCuentaRetornaOk() {
        configurarUsuario();

        var response = controller.desactivarCuenta(usuario);

        verificarRespuesta(
            response,
            HttpStatus.OK,
            "Tu cuenta ha sido desactivada correctamente."
        );

        verify(usuarioService).desactivarCuenta(idUsuario);
    }

    @ParameterizedTest
    @MethodSource("erroresDelServicio")
    void desactivarCuentaRetornaErrorDelServicio(
            RuntimeException error,
            HttpStatus estadoEsperado) {

        configurarUsuario();

        doThrow(error)
            .when(usuarioService)
            .desactivarCuenta(idUsuario);

        var response = controller.desactivarCuenta(usuario);

        verificarRespuesta(response, estadoEsperado, error.getMessage());

        verify(usuarioService).desactivarCuenta(idUsuario);
    }

    private void verificarNoAutorizado(ResponseEntity<?> response) {
        assertAll(
            () -> assertEquals(
                HttpStatus.UNAUTHORIZED, response.getStatusCode()
            ),
            () -> assertNull(response.getBody())
        );
    }

    private void verificarRespuesta(
            ResponseEntity<String> response,
            HttpStatus estadoEsperado,
            String mensajeEsperado) {

        assertAll(
            () -> assertEquals(
                estadoEsperado, response.getStatusCode()
            ),
            () -> assertEquals(mensajeEsperado, response.getBody())
        );
    }
}
