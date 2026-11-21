
package co.edu.unbosque.marketplace.publicacion.api;

import static co.edu.unbosque.marketplace.config.TraductorDeErroresPrueba.traducir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.api.request.RechazarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.RevisionAdministrativaService;
import co.edu.unbosque.marketplace.publicacion.application.dto.DecisionPublicacionResponse;

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

        ResponseEntity<?> respuesta = traducir(() ->
                controller.aprobarPublicacion(null, idPublicacion));

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

        ResponseEntity<?> respuesta = traducir(() ->
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(revisionService).aprobarPublicacion(
                idAdministrador, idPublicacion);
    }

    @Test
    void aprobarPublicacionSinPermisosRetorna403() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new AccesoNoPermitidoException("Acceso denegado."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void aprobarPublicacionInexistenteRetorna404() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new RecursoNoEncontradoException(
                        "Publicación no encontrada."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void aprobarPublicacionConArgumentosInvalidosRetorna400() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new DatosInvalidosException("Datos inválidos."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void aprobarPublicacionEnEstadoInvalidoRetorna409() {

        when(revisionService.aprobarPublicacion(
                idAdministrador, idPublicacion))
                .thenThrow(new ConflictoNegocioException(
                        "La publicación no puede aprobarse."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.aprobarPublicacion(
                        idAdministrador, idPublicacion));

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }

    @Test
    void rechazarPublicacionSinAdministradorRetorna401() {

        ResponseEntity<?> respuesta = traducir(() ->
                controller.rechazarPublicacion(
                        null,
                        idPublicacion,
                        new RechazarPublicacionRequest(),
                        resultadoValidacion
                ));

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

        ResponseEntity<?> respuesta = traducir(() ->
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

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

        ResponseEntity<?> respuesta = traducir(() ->
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

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
        )).thenThrow(new AccesoNoPermitidoException("Acceso denegado."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

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
        )).thenThrow(new RecursoNoEncontradoException(
                "Publicación no encontrada."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

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
        )).thenThrow(new DatosInvalidosException("Motivo inválido."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

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
        )).thenThrow(new ConflictoNegocioException(
                "La publicación no puede rechazarse."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.rechazarPublicacion(
                        idAdministrador,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
