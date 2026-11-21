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
import co.edu.unbosque.marketplace.publicacion.application.PublicacionEstadoService;
import co.edu.unbosque.marketplace.publicacion.application.dto.EstadoPublicacionResponse;

@ExtendWith(MockitoExtension.class)
class PublicacionEstadoControllerTest {

    @Mock
    private PublicacionEstadoService estadoService;

    @InjectMocks
    private PublicacionEstadoController controller;

    private UUID idAnfitrion;
    private UUID idPublicacion;

    @BeforeEach
    void setUp() {
        idAnfitrion = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
    }

    @Test
    void pausarPublicacionSinUsuarioRetorna401() {

        ResponseEntity<?> respuesta = traducir(() ->
                controller.pausarPublicacion(null, idPublicacion));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionCorrectamenteRetorna200() {

        EstadoPublicacionResponse esperado =
                mock(EstadoPublicacionResponse.class);

        when(estadoService.pausarPublicacion(
                idAnfitrion, idPublicacion))
                .thenReturn(esperado);

        ResponseEntity<?> respuesta = traducir(() ->
                controller.pausarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(estadoService).pausarPublicacion(
                idAnfitrion, idPublicacion);
    }

    @Test
    void pausarPublicacionSinPermisosRetorna403() {

        when(estadoService.pausarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new AccesoNoPermitidoException(
                        "No tienes permiso para pausar esta publicación."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.pausarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionInexistenteRetorna404() {

        when(estadoService.pausarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new RecursoNoEncontradoException(
                        "Publicación no encontrada."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.pausarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionConArgumentosInvalidosRetorna400() {

        when(estadoService.pausarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new DatosInvalidosException(
                        "Datos inválidos."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.pausarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionEnEstadoInvalidoRetorna409() {

        when(estadoService.pausarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new ConflictoNegocioException(
                        "La publicación no puede pausarse."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.pausarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionSinUsuarioRetorna401() {

        ResponseEntity<?> respuesta = traducir(() ->
                controller.reactivarPublicacion(
                        null, idPublicacion));

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionCorrectamenteRetorna200() {

        EstadoPublicacionResponse esperado =
                mock(EstadoPublicacionResponse.class);

        when(estadoService.reactivarPublicacion(
                idAnfitrion, idPublicacion))
                .thenReturn(esperado);

        ResponseEntity<?> respuesta = traducir(() ->
                controller.reactivarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());

        verify(estadoService).reactivarPublicacion(
                idAnfitrion, idPublicacion);
    }

    @Test
    void reactivarPublicacionSinPermisosRetorna403() {

        when(estadoService.reactivarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new AccesoNoPermitidoException(
                        "No tienes permiso para reactivar esta publicación."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.reactivarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionInexistenteRetorna404() {

        when(estadoService.reactivarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new RecursoNoEncontradoException(
                        "Publicación no encontrada."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.reactivarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionConArgumentosInvalidosRetorna400() {

        when(estadoService.reactivarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new DatosInvalidosException(
                        "Datos inválidos."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.reactivarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionEnEstadoInvalidoRetorna409() {

        when(estadoService.reactivarPublicacion(
                idAnfitrion, idPublicacion))
                .thenThrow(new ConflictoNegocioException(
                        "La publicación no puede reactivarse."
                ));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.reactivarPublicacion(
                        idAnfitrion, idPublicacion));

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
