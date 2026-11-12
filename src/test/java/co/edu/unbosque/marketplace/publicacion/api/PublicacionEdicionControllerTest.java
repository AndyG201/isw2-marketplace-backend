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

import co.edu.unbosque.marketplace.publicacion.api.request.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionEdicionService;

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

        ResponseEntity<?> respuesta =
                controller.editarPublicacion(
                        null,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

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

        ResponseEntity<?> respuesta =
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

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

        ResponseEntity<?> respuesta =
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

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
        )).thenThrow(new AccessDeniedException(
                "No tienes permiso para editar esta publicación."
        ));

        ResponseEntity<?> respuesta =
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void editarPublicacionInexistenteRetorna404() {

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenThrow(new NoSuchElementException(
                "Publicación no encontrada."
        ));

        ResponseEntity<?> respuesta =
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void editarPublicacionConArgumentosInvalidosRetorna400() {

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenThrow(new IllegalArgumentException(
                "Los datos de la publicación no son válidos."
        ));

        ResponseEntity<?> respuesta =
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void editarPublicacionEnEstadoInvalidoRetorna409() {

        when(resultadoValidacion.hasErrors()).thenReturn(false);

        when(edicionService.actualizarPublicacion(
                idAnfitrion,
                idPublicacion,
                solicitud
        )).thenThrow(new IllegalStateException(
                "La publicación no puede modificarse en su estado actual."
        ));

        ResponseEntity<?> respuesta =
                controller.editarPublicacion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud,
                        resultadoValidacion
                );

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
