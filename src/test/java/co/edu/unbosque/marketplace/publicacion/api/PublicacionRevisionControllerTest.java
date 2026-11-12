package co.edu.unbosque.marketplace.publicacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import co.edu.unbosque.marketplace.publicacion.api.response.EnviarRevisionResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionRevisionService;

@ExtendWith(MockitoExtension.class)
class PublicacionRevisionControllerTest {

    @Mock
    private PublicacionRevisionService revisionService;

    @InjectMocks
    private PublicacionRevisionController controller;

    private UUID idAnfitrion;
    private UUID idPublicacion;

    @BeforeEach
    void setUp() {
        idAnfitrion = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
    }

    @Test
    void enviarRevisionSinUsuarioRetorna401() {

        ResponseEntity<?> respuesta =
                controller.enviarRevision(null, idPublicacion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionCorrectamenteRetorna200() {

        EnviarRevisionResponse esperado =
                mock(EnviarRevisionResponse.class);

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenReturn(esperado);

        ResponseEntity<?> respuesta =
                controller.enviarRevision(
                        idAnfitrion, idPublicacion);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(revisionService).enviarRevision(
                idAnfitrion, idPublicacion);
    }

    @Test
    void enviarRevisionSinPermisosRetorna403() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new AccessDeniedException(
                        "No tienes permiso para enviar esta publicación a revisión."
                ));

        ResponseEntity<?> respuesta =
                controller.enviarRevision(
                        idAnfitrion, idPublicacion);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionInexistenteRetorna404() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new NoSuchElementException(
                        "Publicación no encontrada."
                ));

        ResponseEntity<?> respuesta =
                controller.enviarRevision(
                        idAnfitrion, idPublicacion);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionConArgumentosInvalidosRetorna400() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new IllegalArgumentException(
                        "Datos inválidos."
                ));

        ResponseEntity<?> respuesta =
                controller.enviarRevision(
                        idAnfitrion, idPublicacion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionEnEstadoInvalidoRetorna409() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new IllegalStateException(
                        "La publicación no puede enviarse a revisión en su estado actual."
                ));

        ResponseEntity<?> respuesta =
                controller.enviarRevision(
                        idAnfitrion, idPublicacion);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
