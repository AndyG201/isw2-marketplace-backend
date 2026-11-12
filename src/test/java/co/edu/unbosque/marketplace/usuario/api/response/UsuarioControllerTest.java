package co.edu.unbosque.marketplace.usuario.api.response;

import co.edu.unbosque.marketplace.usuario.api.UsuarioController;
import co.edu.unbosque.marketplace.usuario.api.request.AutenticacionRequest;
import co.edu.unbosque.marketplace.usuario.api.request.RegistrarUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.api.request.ReactivarCuentaRequest;
import co.edu.unbosque.marketplace.usuario.application.AutenticacionService;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AutenticacionService autenticacionService;

    @InjectMocks
    private UsuarioController controller;

    private RegistrarUsuarioRequest solicitudRegistro() {
        RegistrarUsuarioRequest solicitud = new RegistrarUsuarioRequest();
        solicitud.setNombre("Ana");
        solicitud.setApellido("Perez");
        solicitud.setCorreo("ana@ejemplo.com");
        solicitud.setTipoDocumento("CEDULA");
        solicitud.setDocumentoIdentidad("123456789");
        solicitud.setTelefono("3001234567");
        solicitud.setContrasena("Clave123");
        solicitud.setRol("HUESPED");
        return solicitud;
    }

    private AutenticacionRequest solicitudLogin() {
        AutenticacionRequest solicitud = new AutenticacionRequest();
        solicitud.setCorreo("ana@ejemplo.com");
        solicitud.setContrasena("Clave123");
        return solicitud;
    }

    private ReactivarCuentaRequest solicitudReactivacion() {
        ReactivarCuentaRequest solicitud = new ReactivarCuentaRequest();
        solicitud.setCorreo("ana@ejemplo.com");
        solicitud.setContrasena("Clave123");
        return solicitud;
    }

    @Test
    void registrarRetorna201CuandoServicioRetornaUno() {
        RegistrarUsuarioRequest solicitud = solicitudRegistro();

        when(usuarioService.registrar(solicitud)).thenReturn(1);

        var response = controller.registrar(solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.CREATED, response.getStatusCode()
            ),
            () -> assertEquals(
                "Usuario creado. Verifica tu correo para activar el acceso.",
                response.getBody()
            )
        );

        verify(usuarioService).registrar(solicitud);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 2})
    void registrarRetorna400CuandoResultadoEsDistintoDeUno(int resultado) {
        RegistrarUsuarioRequest solicitud = solicitudRegistro();

        when(usuarioService.registrar(solicitud)).thenReturn(resultado);

        var response = controller.registrar(solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.BAD_REQUEST, response.getStatusCode()
            ),
            () -> assertEquals(
                "Datos inválidos o usuario existente.",
                response.getBody()
            )
        );

        verify(usuarioService).registrar(solicitud);
    }

    @Test
    void iniciarSesionRetornaOkConTokenYTipoBearer() {
        AutenticacionRequest solicitud = solicitudLogin();
        String token = "token-prueba";

        when(autenticacionService.iniciarSesion(solicitud))
            .thenReturn(token);

        var response = controller.iniciarSesion(solicitud);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AutenticacionResponse);

        AutenticacionResponse body =
                (AutenticacionResponse) response.getBody();

        assertAll(
            () -> assertEquals(token, body.getToken()),
            () -> assertEquals("Bearer", body.getTipo())
        );

        verify(autenticacionService).iniciarSesion(solicitud);
    }

    @Test
    void iniciarSesionRetorna401AnteIllegalArgumentException() {
        AutenticacionRequest solicitud = solicitudLogin();
        String mensaje = "Credenciales incorrectas.";

        when(autenticacionService.iniciarSesion(solicitud))
            .thenThrow(new IllegalArgumentException(mensaje));

        var response = controller.iniciarSesion(solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.UNAUTHORIZED, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(autenticacionService).iniciarSesion(solicitud);
    }

    @Test
    void iniciarSesionRetorna403AnteIllegalStateException() {
        AutenticacionRequest solicitud = solicitudLogin();
        String mensaje = "La cuenta no tiene permitido iniciar sesión.";

        when(autenticacionService.iniciarSesion(solicitud))
            .thenThrow(new IllegalStateException(mensaje));

        var response = controller.iniciarSesion(solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.FORBIDDEN, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(autenticacionService).iniciarSesion(solicitud);
    }

    @Test
    void reactivarCuentaRetornaOk() {
        ReactivarCuentaRequest solicitud = solicitudReactivacion();

        var response = controller.reactivarCuenta(solicitud);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "Tu cuenta ha sido reactivada correctamente. "
                    + "Inicia sesión nuevamente.",
                response.getBody()
            )
        );

        verify(usuarioService).reactivarCuenta(solicitud);
    }

    @Test
    void reactivarCuentaRetorna401AnteIllegalArgumentException() {
        ReactivarCuentaRequest solicitud = solicitudReactivacion();
        String mensaje = "Credenciales incorrectas.";

        doThrow(new IllegalArgumentException(mensaje))
            .when(usuarioService).reactivarCuenta(solicitud);

        var response = controller.reactivarCuenta(solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.UNAUTHORIZED, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(usuarioService).reactivarCuenta(solicitud);
    }

    @Test
    void reactivarCuentaRetorna403AnteIllegalStateException() {
        ReactivarCuentaRequest solicitud = solicitudReactivacion();
        String mensaje = "La cuenta no puede reactivarse.";

        doThrow(new IllegalStateException(mensaje))
            .when(usuarioService).reactivarCuenta(solicitud);

        var response = controller.reactivarCuenta(solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.FORBIDDEN, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(usuarioService).reactivarCuenta(solicitud);
    }
}
