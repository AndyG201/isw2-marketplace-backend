package co.edu.unbosque.marketplace.cancelacion.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.cancelacion.api.response.PoliticaCancelacionResponse;
import co.edu.unbosque.marketplace.cancelacion.application.PoliticaCancelacionService;

class PoliticaCancelacionControllerTest {

    private PoliticaCancelacionService politicaService;
    private PoliticaCancelacionController controller;

    @BeforeEach
    void setUp() {
        politicaService = mock(PoliticaCancelacionService.class);
        controller = new PoliticaCancelacionController(politicaService);
    }

    @Test
    void debeConsultarPoliticasActivasCorrectamente() {
        List<PoliticaCancelacionResponse> politicas = List.of(
                new PoliticaCancelacionResponse(
                        UUID.randomUUID(),
                        "Cancelación flexible",
                        1,
                        "Permite cancelar con devolución.",
                        48,
                        new BigDecimal("100.00")
                ),
                new PoliticaCancelacionResponse(
                        UUID.randomUUID(),
                        "Cancelación moderada",
                        2,
                        "Permite devolución parcial.",
                        24,
                        new BigDecimal("50.00")
                )
        );

        when(politicaService.consultarPoliticasActivas())
                .thenReturn(politicas);

        ResponseEntity<List<PoliticaCancelacionResponse>> respuesta =
                controller.consultarPoliticasActivas();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals(2, respuesta.getBody().size());
        assertEquals("Cancelación flexible",
                respuesta.getBody().get(0).getNombre());
        assertEquals("Cancelación moderada",
                respuesta.getBody().get(1).getNombre());

        verify(politicaService, times(1)).consultarPoliticasActivas();
    }

    @Test
    void debeRetornarListaVaciaCuandoNoHayPoliticasActivas() {
        when(politicaService.consultarPoliticasActivas())
                .thenReturn(List.of());

        ResponseEntity<List<PoliticaCancelacionResponse>> respuesta =
                controller.consultarPoliticasActivas();

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertTrue(respuesta.getBody().isEmpty());

        verify(politicaService, times(1)).consultarPoliticasActivas();
    }

    @Test
    void debeRetornarLaListaCompletaSinModificarla() {
        List<PoliticaCancelacionResponse> politicas = List.of(
                new PoliticaCancelacionResponse(
                        UUID.randomUUID(),
                        "Cancelación estricta",
                        3,
                        "No permite devolución.",
                        72,
                        BigDecimal.ZERO
                )
        );

        when(politicaService.consultarPoliticasActivas())
                .thenReturn(politicas);

        ResponseEntity<List<PoliticaCancelacionResponse>> respuesta =
                controller.consultarPoliticasActivas();

        assertSame(politicas, respuesta.getBody());

        verify(politicaService).consultarPoliticasActivas();
    }

    @Test
    void debePropagarExcepcionCuandoElServicioFalla() {
        when(politicaService.consultarPoliticasActivas())
                .thenThrow(new IllegalStateException("Error al consultar políticas"));

        assertThrows(
                IllegalStateException.class,
                () -> controller.consultarPoliticasActivas()
        );

        verify(politicaService).consultarPoliticasActivas();
    }
}
