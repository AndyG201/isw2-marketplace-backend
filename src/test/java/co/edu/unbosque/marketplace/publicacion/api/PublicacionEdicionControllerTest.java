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
import co.edu.unbosque.marketplace.publicacion.application.PublicacionEdicionService;
import co.edu.unbosque.marketplace.publicacion.application.dto.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionAlojamientoResponse;

@ExtendWith(MockitoExtension.class)
class PublicacionEdicionControllerTest {

    @Mock
    private PublicacionEdicionService edicionService;

    @Mock
    private BindingResult resultadoValidacion;

    @Mock
    private EditarPublicacionRequest solicitud;

    @InjectMocks
    private PublicacionEdicionController controller;

    private UUID idAnfitrion;
    private UUID idPublicacion;

    @BeforeEach
    void setUp() {
        idAnfitrion = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
    }

    @Test
    void editarPublicacionSinUsuarioRetorna401() {

        ResponseEntity<?> respuesta = traducir(() ->
                controller.editarPublicacion(
                        null,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());

        verifyNoInteractions(edicionService);
    }

    @Test
    void editarPublicacionConErroresDeValidacionRetorna400() {

        when(resultadoValidacion.hasErrors()).thenReturn(true);

        when(resultadoValidacion.getFieldErrors()).thenReturn(
                List.of(new FieldError(
                        "solicitud",
                        "titulo",
                        "El título es obligatorio."
                ))
        );

        ResponseEntity<?> respuesta = traducir(() ->
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());

        verifyNoInteractions(edicionService);
    }

    @Test
    void editarPublicacionCorrectamenteRetorna200() {

        PublicacionAlojamientoResponse esperado =
                mock(PublicacionAlojamientoResponse.class);

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenReturn(esperado);

        ResponseEntity<?> respuesta = traducir(() ->
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(edicionService).actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        );
    }

    @Test
    void editarPublicacionSinPermisosRetorna403() {

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenThrow(new AccesoNoPermitidoException(
                "No tienes permiso para editar esta publicación."
        ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void editarPublicacionInexistenteRetorna404() {

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenThrow(new RecursoNoEncontradoException(
                "Publicación no encontrada."
        ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void editarPublicacionConArgumentosInvalidosRetorna400() {

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenThrow(new DatosInvalidosException(
                "Los datos de la publicación no son válidos."
        ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void editarPublicacionEnEstadoInvalidoRetorna409() {

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenThrow(new ConflictoNegocioException(
                "La publicación no puede modificarse en su estado actual."
        ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                ));

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
