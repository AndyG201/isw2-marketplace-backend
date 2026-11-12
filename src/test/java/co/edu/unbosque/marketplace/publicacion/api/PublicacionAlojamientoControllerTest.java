package co.edu.unbosque.marketplace.publicacion.api;

import static org.junit.jupiter.api.Assertions.assertAll;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import co.edu.unbosque.marketplace.publicacion.api.request.CrearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionAlojamientoService;

@ExtendWith(MockitoExtension.class)
class PublicacionAlojamientoControllerTest {

    @Mock
    private PublicacionAlojamientoService publicacionService;

    @Mock
    private CrearPublicacionRequest solicitud;

    @Mock
    private BindingResult resultadoValidacion;

    @InjectMocks
    private PublicacionAlojamientoController controller;

    @Test
    void testCrearPublicacionSinUsuario() {

        ResponseEntity<?> respuesta = controller.crearPublicacion(
                null,
                solicitud,
                resultadoValidacion
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                respuesta.getStatusCode()
        );

        verifyNoInteractions(publicacionService);
    }

    @Test
    void testCrearPublicacionConErroresDeValidacion() {

        UUID idUsuario = UUID.randomUUID();

        FieldError error = new FieldError(
                "solicitud",
                "titulo",
                "El título es obligatorio."
        );

        when(resultadoValidacion.hasErrors()).thenReturn(true);
        when(resultadoValidacion.getFieldErrors())
                .thenReturn(List.of(error));

        ResponseEntity<?> respuesta = controller.crearPublicacion(
                idUsuario,
                solicitud,
                resultadoValidacion
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode()
        );

        assertEquals(
                "El título es obligatorio.",
                ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );

        verifyNoInteractions(publicacionService);
    }

    @Test
    void testCrearPublicacionCorrectamente() {

        UUID idUsuario = UUID.randomUUID();

        PublicacionAlojamientoResponse publicacion =
                new PublicacionAlojamientoResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Apartamento en Bogotá",
                        "BORRADOR",
                        List.of()
                );

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(publicacionService.crearPublicacion(idUsuario, solicitud))
                .thenReturn(publicacion);

        ResponseEntity<?> respuesta = controller.crearPublicacion(
                idUsuario,
                solicitud,
                resultadoValidacion
        );

        assertAll(
                () -> assertEquals(
                        HttpStatus.CREATED,
                        respuesta.getStatusCode()
                ),
                () -> assertEquals(
                        publicacion,
                        respuesta.getBody()
                )
        );

        verify(publicacionService).crearPublicacion(
                idUsuario,
                solicitud
        );
    }

    @Test
    void testCrearPublicacionConArgumentosInvalidos() {

        UUID idUsuario = UUID.randomUUID();

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(publicacionService.crearPublicacion(idUsuario, solicitud))
                .thenThrow(
                        new IllegalArgumentException(
                                "Datos incorrectos."
                        )
                );

        ResponseEntity<?> respuesta = controller.crearPublicacion(
                idUsuario,
                solicitud,
                resultadoValidacion
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                respuesta.getStatusCode()
        );

        assertEquals(
                "Datos incorrectos.",
                ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );
    }

    @Test
    void testCrearPublicacionConConflicto() {

        UUID idUsuario = UUID.randomUUID();

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(publicacionService.crearPublicacion(idUsuario, solicitud))
                .thenThrow(
                        new IllegalStateException(
                                "No es posible crear la publicación."
                        )
                );

        ResponseEntity<?> respuesta = controller.crearPublicacion(
                idUsuario,
                solicitud,
                resultadoValidacion
        );

        assertEquals(
                HttpStatus.CONFLICT,
                respuesta.getStatusCode()
        );

        assertEquals(
                "No es posible crear la publicación.",
                ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );
    }
}
