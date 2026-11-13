
package co.edu.unbosque.marketplace.publicacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import co.edu.unbosque.marketplace.publicacion.api.request.RechazarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.DecisionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.RevisionAdministrativaService;

@ExtendWith(MockitoExtension.class)
class PublicacionDecisionControllerTest {

    @Mock
    private RevisionAdministrativaService revisionService;

    @Mock
    private BindingResult resultadoValidacion;

    @InjectMocks
    private PublicacionDecisionController controller;

    private UUID idAdministrador;
    private UUID idPublicacion;

    @BeforeEach
    void setUp() {
        idAdministrador = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
    }

    @Test
    void aprobarPublicacionSinAdministradorRetorna401() {

        ResponseEntity<?> respuesta =
                controller.aprobarPublicacion(null, idPublicacion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());

        verifyNoInteractions(revisionService);
    }

    @Test
    void aprobarPublicacionCorrectamenteRetorna200() {

        DecisionPublicacionResponse esperado =
                mock(DecisionPublicacionResponse.class);

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenReturn(esperado);

        ResponseEntity<?> respuesta =
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(revisionService).aprobarPublicacion(
                idAdministrador, idPublicacion);
    }

    @Test
    void aprobarPublicacionSinPermisosRetorna403() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new AccessDeniedException("Acceso denegado."));

        ResponseEntity<?> respuesta =
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void aprobarPublicacionInexistenteRetorna404() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new NoSuchElementException(
                        "Publicación no encontrada."));

        ResponseEntity<?> respuesta =
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void aprobarPublicacionConArgumentosInvalidosRetorna400() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new IllegalArgumentException("Datos inválidos."));

        ResponseEntity<?> respuesta =
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void aprobarPublicacionEnEstadoInvalidoRetorna409() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new IllegalStateException(
                        "La publicación no puede aprobarse."));

        ResponseEntity<?> respuesta =
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }

    @Test
    void rechazarPublicacionSinAdministradorRetorna401() {

        ResponseEntity<?> respuesta =
                controller.rechazarPublicacion(
                        null,
                        idPublicacion,
                        new RechazarPublicacionRequest(),
                        resultadoValidacion
                );

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());

        verifyNoInteractions(revisionService);
    }

    @Test
    void rechazarPublicacionConErroresDeValidacionRetorna400() {

        RechazarPublicacionRequest solicitud =
                new RechazarPublicacionRequest();

        when(resultadoValidacion.hasErrors()).thenReturn(true);

        when(resultadoValidacion.getFieldErrors()).thenReturn(
                List.of(new FieldError(
                        "solicitud",
                        "motivo",
                        "El motivo es obligatorio."
                ))
        );

        ResponseEntity<?> respuesta =
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());

        verifyNoInteractions(revisionService);
    }

    @Test
    void rechazarPublicacionCorrectamenteRetorna200() {

        RechazarPublicacionRequest solicitud =
                new RechazarPublicacionRequest();

        solicitud.setMotivo("La información está incompleta.");

        DecisionPublicacionResponse esperado =
                mock(DecisionPublicacionResponse.class);

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(revisionService.rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                solicitud.getMotivo()
        )).thenReturn(esperado);

        ResponseEntity<?> respuesta =
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(revisionService).rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                solicitud.getMotivo()
        );
    }

    @Test
    void rechazarPublicacionSinPermisosRetorna403() {

        RechazarPublicacionRequest solicitud =
                new RechazarPublicacionRequest();

        solicitud.setMotivo("Motivo de rechazo.");

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(revisionService.rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                solicitud.getMotivo()
        )).thenThrow(new AccessDeniedException("Acceso denegado."));

        ResponseEntity<?> respuesta =
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void rechazarPublicacionInexistenteRetorna404() {

        RechazarPublicacionRequest solicitud =
                new RechazarPublicacionRequest();

        solicitud.setMotivo("Motivo de rechazo.");

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(revisionService.rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                solicitud.getMotivo()
        )).thenThrow(new NoSuchElementException(
                "Publicación no encontrada."));

        ResponseEntity<?> respuesta =
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void rechazarPublicacionConArgumentosInvalidosRetorna400() {

        RechazarPublicacionRequest solicitud =
                new RechazarPublicacionRequest();

        solicitud.setMotivo("Motivo de rechazo.");

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(revisionService.rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                solicitud.getMotivo()
        )).thenThrow(new IllegalArgumentException("Motivo inválido."));

        ResponseEntity<?> respuesta =
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void rechazarPublicacionEnEstadoInvalidoRetorna409() {

        RechazarPublicacionRequest solicitud =
                new RechazarPublicacionRequest();

        solicitud.setMotivo("Motivo de rechazo.");

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(revisionService.rechazarPublicacion(
                idAdministrador,
                idPublicacion,
                solicitud.getMotivo()
        )).thenThrow(new IllegalStateException(
                "La publicación no puede rechazarse."));

        ResponseEntity<?> respuesta =
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
