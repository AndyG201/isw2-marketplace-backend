package co.edu.unbosque.marketplace.publicacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionAdministracionResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionDetallePublicoService;

@ExtendWith(MockitoExtension.class)
class PublicacionAdministrativaDetalleControllerTest {

    @Mock
    private PublicacionDetallePublicoService detalleService;

    @InjectMocks
    private PublicacionAdministrativaDetalleController controller;

    @Test
    void devuelveElDetalleParaRevisarLaPublicacion() {
        UUID id = UUID.randomUUID();
        PublicacionAdministracionResponse detalle = new PublicacionAdministracionResponse(
                mock(PublicacionDetalleResponse.class), List.of(), null, null);
        when(detalleService.consultarParaAdministracion(id)).thenReturn(detalle);

        ResponseEntity<?> respuesta = controller.consultar(id);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(detalle, respuesta.getBody());
    }

    @Test
    void respondeNoEncontradaSiLaPublicacionNoExiste() {
        UUID id = UUID.randomUUID();
        when(detalleService.consultarParaAdministracion(id))
                .thenThrow(new NoSuchElementException("La publicación no existe."));

        ResponseEntity<?> respuesta = controller.consultar(id);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("La publicación no existe.", ((Map<?, ?>) respuesta.getBody()).get("mensaje"));
    }
}
