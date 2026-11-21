package co.edu.unbosque.marketplace.publicacion.api;

import static co.edu.unbosque.marketplace.config.TraductorDeErroresPrueba.traducir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionRevisionService;
import co.edu.unbosque.marketplace.publicacion.application.dto.EnviarRevisionResponse;

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

        ResponseEntity<?> respuesta = traducir(() ->
                controller.enviarRevision(null, idPublicacion));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionCorrectamenteRetorna200() {

        EnviarRevisionResponse esperado =
                mock(EnviarRevisionResponse.class);

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenReturn(esperado);

        ResponseEntity<?> respuesta = traducir(() ->
                controller.enviarRevision(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(revisionService).enviarRevision(
                idAnfitrion, idPublicacion);
    }

    @Test
    void enviarRevisionSinPermisosRetorna403() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new AccesoNoPermitidoException(
                        "No tienes permiso para enviar esta publicación a revisión."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.enviarRevision(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionInexistenteRetorna404() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new RecursoNoEncontradoException(
                        "Publicación no encontrada."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.enviarRevision(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionConArgumentosInvalidosRetorna400() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new DatosInvalidosException(
                        "Datos inválidos."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.enviarRevision(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void enviarRevisionEnEstadoInvalidoRetorna409() {

        when(revisionService.enviarRevision(
                idAnfitrion, idPublicacion))
                .thenThrow(new ConflictoNegocioException(
                        "La publicación no puede enviarse a revisión en su estado actual."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.enviarRevision(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
