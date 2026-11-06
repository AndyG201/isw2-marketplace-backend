package co.edu.unbosque.marketplace.usuario.api.response;

import java.util.Map;

import co.edu.unbosque.marketplace.usuario.api.RecuperacionContrasenaController;
import co.edu.unbosque.marketplace.usuario.api.request.RestablecerContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.api.request.SolicitarRecuperacionRequest;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacionContrasenaControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private RecuperacionContrasenaController controller;

    private SolicitarRecuperacionRequest solicitudRecuperacion() {
        SolicitarRecuperacionRequest solicitud =
                new SolicitarRecuperacionRequest();

        solicitud.setCorreo("  usuario@ejemplo.com  ");
        return solicitud;
    }

    private RestablecerContrasenaRequest solicitudRestablecimiento() {
        RestablecerContrasenaRequest solicitud =
                new RestablecerContrasenaRequest();

        solicitud.setCorreo("  usuario@ejemplo.com  ");
        solicitud.setCodigo("123456");
        solicitud.setNuevaContrasena("NuevaClave123");
        return solicitud;
    }

    @Test
    void solicitarRetornaOkYEliminaEspaciosDelCorreo() {
        var response = controller.solicitar(solicitudRecuperacion());

        verificarRespuesta(
            response,
            HttpStatus.OK,
            "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña."
        );

        verify(usuarioService).generarCodigoRecuperacion(
            "usuario@ejemplo.com"
        );
    }

    @Test
    void solicitarRetorna400CuandoSolicitudEsNula() {
        var response = controller.solicitar(null);

        verificarRespuesta(
            response,
            HttpStatus.BAD_REQUEST,
            "Solicitud inválida."
        );

        verifyNoInteractions(usuarioService);
    }

    @Test
    void solicitarRetorna500CuandoServicioLanzaExcepcion() {
        doThrow(new RuntimeException("Error del servicio."))
            .when(usuarioService)
            .generarCodigoRecuperacion("usuario@ejemplo.com");

        var response = controller.solicitar(solicitudRecuperacion());

        verificarRespuesta(
            response,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "No fue posible procesar la solicitud en este momento."
        );

        verify(usuarioService).generarCodigoRecuperacion(
            "usuario@ejemplo.com"
        );
    }

    @Test
    void restablecerRetornaOkCuandoServicioRetornaTrue() {
        when(usuarioService.restablecerContrasena(
            "usuario@ejemplo.com",
            "123456",
            "NuevaClave123"
        )).thenReturn(true);

        var response = controller.restablecer(
            solicitudRestablecimiento()
        );

        verificarRespuesta(
            response,
            HttpStatus.OK,
            "La contraseña se ha actualizado correctamente."
        );

        verify(usuarioService).restablecerContrasena(
            "usuario@ejemplo.com",
            "123456",
            "NuevaClave123"
        );
    }

    @Test
    void restablecerRetorna400CuandoServicioRetornaFalse() {
        when(usuarioService.restablecerContrasena(
            "usuario@ejemplo.com",
            "123456",
            "NuevaClave123"
        )).thenReturn(false);

        var response = controller.restablecer(
            solicitudRestablecimiento()
        );

        verificarRespuesta(
            response,
            HttpStatus.BAD_REQUEST,
            "El código es inválido, ha vencido o no se pudo completar la recuperación."
        );

        verify(usuarioService).restablecerContrasena(
            "usuario@ejemplo.com",
            "123456",
            "NuevaClave123"
        );
    }

    @Test
    void restablecerRetorna400CuandoSolicitudEsNula() {
        var response = controller.restablecer(null);

        verificarRespuesta(
            response,
            HttpStatus.BAD_REQUEST,
            "Solicitud inválida."
        );

        verifyNoInteractions(usuarioService);
    }

    @Test
    void restablecerPropagaExcepcionDelServicio() {
        RuntimeException error =
                new RuntimeException("Error del servicio.");

        when(usuarioService.restablecerContrasena(
            "usuario@ejemplo.com",
            "123456",
            "NuevaClave123"
        )).thenThrow(error);

        RuntimeException excepcion = assertThrows(
            RuntimeException.class,
            () -> controller.restablecer(solicitudRestablecimiento())
        );

        assertSame(error, excepcion);

        verify(usuarioService).restablecerContrasena(
            "usuario@ejemplo.com",
            "123456",
            "NuevaClave123"
        );
    }

    private void verificarRespuesta(
            ResponseEntity<Map<String, String>> response,
            HttpStatus estadoEsperado,
            String mensajeEsperado) {

        assertAll(
            () -> assertEquals(
                estadoEsperado, response.getStatusCode()
            ),
            () -> assertEquals(
                Map.of("mensaje", mensajeEsperado),
                response.getBody()
            )
        );
    }
}
