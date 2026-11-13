package co.edu.unbosque.marketplace.publicacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.publicacion.api.response.MisPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionConsultaService;

@ExtendWith(MockitoExtension.class)
class PublicacionConsultaControllerTest {

    @Mock
    private PublicacionConsultaService consultaService;

    @InjectMocks
    private PublicacionConsultaController controller;

    @Test
    void testConsultarMisPublicacionesSinUsuario() {

        ResponseEntity<?> respuesta =
                controller.consultarMisPublicaciones(null);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                respuesta.getStatusCode()
        );

        assertEquals(
                "Debes iniciar sesión para consultar tus publicaciones.",
                ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );

        verifyNoInteractions(consultaService);
    }

    @Test
    void testConsultarMisPublicacionesCorrectamente() {

        UUID idUsuario = UUID.randomUUID();

        MisPublicacionesResponse publicaciones =
                new MisPublicacionesResponse(
                        "Publicaciones consultadas correctamente.",
                        List.of()
                );

        when(consultaService.consultarMisPublicaciones(idUsuario))
                .thenReturn(publicaciones);

        ResponseEntity<?> respuesta =
                controller.consultarMisPublicaciones(idUsuario);

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertEquals(
                publicaciones,
                respuesta.getBody()
        );

        verify(consultaService).consultarMisPublicaciones(idUsuario);
    }

    @Test
    void testConsultarDetalleSinUsuario() {

        UUID idPublicacion = UUID.randomUUID();

        ResponseEntity<?> respuesta =
                controller.consultarDetalle(null, idPublicacion);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                respuesta.getStatusCode()
        );

        assertEquals(
                "Debes iniciar sesión para consultar esta publicación.",
                ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );

        verifyNoInteractions(consultaService);
    }

    @Test
    void testConsultarDetalleCorrectamente() {

        UUID idUsuario = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();

        PublicacionDetalleResponse detalle = null;

        when(consultaService.consultarDetalle(idUsuario, idPublicacion))
                .thenReturn(detalle);

        ResponseEntity<?> respuesta =
                controller.consultarDetalle(idUsuario, idPublicacion);

        assertEquals(
                HttpStatus.OK,
                respuesta.getStatusCode()
        );

        assertEquals(
                detalle,
                respuesta.getBody()
        );

        verify(consultaService).consultarDetalle(
                idUsuario,
                idPublicacion
        );
    }

    @Test
    void testConsultarDetalleNoEncontrado() {

        UUID idUsuario = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();

        when(consultaService.consultarDetalle(idUsuario, idPublicacion))
                .thenThrow(
                        new IllegalArgumentException(
                                "La publicación no fue encontrada."
                        )
                );

        ResponseEntity<?> respuesta =
                controller.consultarDetalle(idUsuario, idPublicacion);

        assertEquals(
                HttpStatus.NOT_FOUND,
                respuesta.getStatusCode()
        );

        assertEquals(
                "La publicación no fue encontrada.",
                ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );
    }
}
