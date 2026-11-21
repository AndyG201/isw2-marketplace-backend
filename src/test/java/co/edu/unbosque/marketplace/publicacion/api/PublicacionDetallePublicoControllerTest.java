package co.edu.unbosque.marketplace.publicacion.api;

import static co.edu.unbosque.marketplace.config.TraductorDeErroresPrueba.traducir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
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

import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionDetallePublicoService;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionPublicaResponse;

@ExtendWith(MockitoExtension.class)
class PublicacionDetallePublicoControllerTest {

    @Mock
    private PublicacionDetallePublicoService detallePublicoService;

    @InjectMocks
    private PublicacionDetallePublicoController controller;

    @Test
    void devuelveElDetallePublico() {
        UUID id = UUID.randomUUID();
        PublicacionPublicaResponse detalle = new PublicacionPublicaResponse(
                mock(PublicacionDetalleResponse.class), List.of(), null);
        when(detallePublicoService.consultar(id)).thenReturn(detalle);

        ResponseEntity<?> respuesta = traducir(() ->
                controller.consultar(id));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(detalle, respuesta.getBody());
    }

    @Test
    void respondeNoEncontradaSiNoEstaDisponible() {
        UUID id = UUID.randomUUID();
        when(detallePublicoService.consultar(id))
                .thenThrow(new RecursoNoEncontradoException("La publicación no existe o no está disponible."));

        ResponseEntity<?> respuesta = traducir(() ->
                controller.consultar(id));

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("La publicación no existe o no está disponible.",
                ((Map<?, ?>) respuesta.getBody()).get("mensaje"));
    }
}
