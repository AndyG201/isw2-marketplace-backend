package co.edu.unbosque.marketplace.cancelacion.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PoliticaCancelacionResponseTest {

    @Test
    void debeCrearRespuestaConTodosLosAtributos() {
        UUID idPolitica = UUID.randomUUID();
        BigDecimal porcentaje = new BigDecimal("75.50");

        PoliticaCancelacionResponse respuesta =
                new PoliticaCancelacionResponse(
                        idPolitica,
                        "Cancelación flexible",
                        1,
                        "Permite cancelar con devolución parcial.",
                        48,
                        porcentaje
                );

        assertEquals(idPolitica, respuesta.getIdPolitica());
        assertEquals("Cancelación flexible", respuesta.getNombre());
        assertEquals(1, respuesta.getVersion());
        assertEquals(
                "Permite cancelar con devolución parcial.",
                respuesta.getDescripcion()
        );
        assertEquals(48, respuesta.getAnticipacionHoras());
        assertEquals(0, porcentaje.compareTo(respuesta.getPorcentajeDevolucion()));
    }

    @Test
    void debePermitirValoresNulosEnAtributosDeReferencia() {
        PoliticaCancelacionResponse respuesta =
                new PoliticaCancelacionResponse(
                        null,
                        null,
                        0,
                        null,
                        null,
                        null
                );

        assertNull(respuesta.getIdPolitica());
        assertNull(respuesta.getNombre());
        assertEquals(0, respuesta.getVersion());
        assertNull(respuesta.getDescripcion());
        assertNull(respuesta.getAnticipacionHoras());
        assertNull(respuesta.getPorcentajeDevolucion());
    }

    @Test
    void debeConservarValoresNumericosCero() {
        PoliticaCancelacionResponse respuesta =
                new PoliticaCancelacionResponse(
                        UUID.randomUUID(),
                        "Sin devolución",
                        0,
                        "Política sin devolución.",
                        0,
                        BigDecimal.ZERO
                );

        assertEquals(0, respuesta.getVersion());
        assertEquals(0, respuesta.getAnticipacionHoras());
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(respuesta.getPorcentajeDevolucion())
        );
    }

    @Test
    void debeConservarPorcentajeDevolucionDecimal() {
        BigDecimal porcentaje = new BigDecimal("87.25");

        PoliticaCancelacionResponse respuesta =
                new PoliticaCancelacionResponse(
                        UUID.randomUUID(),
                        "Cancelación moderada",
                        2,
                        "Política de devolución moderada.",
                        24,
                        porcentaje
                );

        assertEquals(
                0,
                porcentaje.compareTo(respuesta.getPorcentajeDevolucion())
        );
    }

    @Test
    void debeConservarVersionYAnticipacionHoras() {
        PoliticaCancelacionResponse respuesta =
                new PoliticaCancelacionResponse(
                        UUID.randomUUID(),
                        "Cancelación estricta",
                        3,
                        "Política estricta.",
                        72,
                        new BigDecimal("25.00")
                );

        assertEquals(3, respuesta.getVersion());
        assertEquals(72, respuesta.getAnticipacionHoras());
    }
}
