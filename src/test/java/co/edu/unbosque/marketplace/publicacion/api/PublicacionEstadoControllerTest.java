package co.edu.unbosque.marketplace.publicacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

import co.edu.unbosque.marketplace.publicacion.api.response.EstadoPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionEstadoService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class PublicacionEstadoControllerTest {

    @Mock
    private PublicacionEstadoService estadoService;

    @Mock
    private Usuario usuario;

    @InjectMocks
    private PublicacionEstadoController controller;

    private UUID idPublicacion;

    @BeforeEach
    void setUp() {
        idPublicacion = UUID.randomUUID();
    }

    @Test
    void pausarPublicacionSinUsuarioRetorna401() {
        ResponseEntity<?> respuesta =
                controller.pausarPublicacion(null, idPublicacion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionCorrectamenteRetorna200() {
        EstadoPublicacionResponse esperado =
                org.mockito.Mockito.mock(EstadoPublicacionResponse.class);

        when(estadoService.pausarPublicacion(usuario, idPublicacion))
                .thenReturn(esperado);

        ResponseEntity<?> respuesta =
                controller.pausarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());
        verify(estadoService).pausarPublicacion(usuario, idPublicacion);
    }

    @Test
    void pausarPublicacionSinPermisosRetorna403() {
        when(estadoService.pausarPublicacion(usuario, idPublicacion))
                .thenThrow(new AccessDeniedException("No tienes permiso para pausar esta publicación."));

        ResponseEntity<?> respuesta =
                controller.pausarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionInexistenteRetorna404() {
        when(estadoService.pausarPublicacion(usuario, idPublicacion))
                .thenThrow(new NoSuchElementException("Publicación no encontrada."));

        ResponseEntity<?> respuesta =
                controller.pausarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionConArgumentosInvalidosRetorna400() {
        when(estadoService.pausarPublicacion(usuario, idPublicacion))
                .thenThrow(new IllegalArgumentException("Datos inválidos."));

        ResponseEntity<?> respuesta =
                controller.pausarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void pausarPublicacionEnEstadoInvalidoRetorna409() {
        when(estadoService.pausarPublicacion(usuario, idPublicacion))
                .thenThrow(new IllegalStateException("La publicación no puede pausarse."));

        ResponseEntity<?> respuesta =
                controller.pausarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionSinUsuarioRetorna401() {
        ResponseEntity<?> respuesta =
                controller.reactivarPublicacion(null, idPublicacion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionCorrectamenteRetorna200() {
        EstadoPublicacionResponse esperado =
                org.mockito.Mockito.mock(EstadoPublicacionResponse.class);

        when(estadoService.reactivarPublicacion(usuario, idPublicacion))
                .thenReturn(esperado);

        ResponseEntity<?> respuesta =
                controller.reactivarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());
        verify(estadoService).reactivarPublicacion(usuario, idPublicacion);
    }

    @Test
    void reactivarPublicacionSinPermisosRetorna403() {
        when(estadoService.reactivarPublicacion(usuario, idPublicacion))
                .thenThrow(new AccessDeniedException("No tienes permiso para reactivar esta publicación."));

        ResponseEntity<?> respuesta =
                controller.reactivarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionInexistenteRetorna404() {
        when(estadoService.reactivarPublicacion(usuario, idPublicacion))
                .thenThrow(new NoSuchElementException("Publicación no encontrada."));

        ResponseEntity<?> respuesta =
                controller.reactivarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionConArgumentosInvalidosRetorna400() {
        when(estadoService.reactivarPublicacion(usuario, idPublicacion))
                .thenThrow(new IllegalArgumentException("Datos inválidos."));

        ResponseEntity<?> respuesta =
                controller.reactivarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    }

    @Test
    void reactivarPublicacionEnEstadoInvalidoRetorna409() {
        when(estadoService.reactivarPublicacion(usuario, idPublicacion))
                .thenThrow(new IllegalStateException("La publicación no puede reactivarse."));

        ResponseEntity<?> respuesta =
                controller.reactivarPublicacion(usuario, idPublicacion);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    }
}
