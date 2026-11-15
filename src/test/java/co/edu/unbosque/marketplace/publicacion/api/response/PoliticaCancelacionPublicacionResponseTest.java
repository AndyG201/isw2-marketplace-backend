package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PoliticaCancelacionPublicacionResponseTest {

    @Test
    void testConstructorYGetters() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idPolitica = UUID.randomUUID();
        BigDecimal porcentaje = new BigDecimal("80.00");

        PoliticaCancelacionPublicacionResponse response =
                new PoliticaCancelacionPublicacionResponse(
                        idPublicacion,
                        idPolitica,
                        "Flexible",
                        1,
                        "Permite cancelaciones anticipadas.",
                        48,
                        porcentaje,
                        true
                );

        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals(idPolitica, response.getIdPolitica());
        assertEquals("Flexible", response.getNombre());
        assertEquals(1, response.getVersion());
        assertEquals("Permite cancelaciones anticipadas.", response.getDescripcion());
        assertEquals(48, response.getAnticipacionHoras());
        assertEquals(porcentaje, response.getPorcentajeDevolucion());
        assertTrue(response.isActiva());
    }

    @Test
    void testPoliticaInactiva() {
        PoliticaCancelacionPublicacionResponse response =
                new PoliticaCancelacionPublicacionResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Estricta",
                        2,
                        "Política de cancelación estricta.",
                        24,
                        new BigDecimal("50.00"),
                        false
                );

        assertFalse(response.isActiva());
    }
}
