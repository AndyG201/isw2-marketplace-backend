package co.edu.unbosque.marketplace.cancelacion.contracts;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PoliticaCancelacionInfoTest {

    @Test
    void debeCrearRegistroYConsultarSusAtributos() {
        UUID idPolitica = UUID.randomUUID();
        BigDecimal porcentaje = new BigDecimal("85.50");

        PoliticaCancelacionInfo info = new PoliticaCancelacionInfo(
                idPolitica,
                "Flexible",
                1,
                "Permite cancelaciones con devolución.",
                48,
                porcentaje,
                true
        );

        assertEquals(idPolitica, info.idPolitica());
        assertEquals("Flexible", info.nombre());
        assertEquals(1, info.version());
        assertEquals(
                "Permite cancelaciones con devolución.",
                info.descripcion()
        );
        assertEquals(48, info.anticipacionHoras());
        assertEquals(0, porcentaje.compareTo(info.porcentajeDevolucion()));
        assertTrue(info.activa());
    }

    @Test
    void debeCompararRegistrosConLosMismosDatos() {
        UUID idPolitica = UUID.randomUUID();

        PoliticaCancelacionInfo info1 = new PoliticaCancelacionInfo(
                idPolitica,
                "Flexible",
                1,
                "Descripción.",
                48,
                new BigDecimal("100.00"),
                true
        );

        PoliticaCancelacionInfo info2 = new PoliticaCancelacionInfo(
                idPolitica,
                "Flexible",
                1,
                "Descripción.",
                48,
                new BigDecimal("100.00"),
                true
        );

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    void debeDetectarRegistrosConDatosDiferentes() {
        PoliticaCancelacionInfo info1 = new PoliticaCancelacionInfo(
                UUID.randomUUID(),
                "Flexible",
                1,
                "Descripción.",
                48,
                new BigDecimal("100.00"),
                true
        );

        PoliticaCancelacionInfo info2 = new PoliticaCancelacionInfo(
                UUID.randomUUID(),
                "Estricta",
                2,
                "Otra descripción.",
                24,
                new BigDecimal("50.00"),
                false
        );

        assertNotEquals(info1, info2);
    }

    @Test
    void debePermitirValoresNulosEnAtributosDeReferencia() {
        PoliticaCancelacionInfo info = new PoliticaCancelacionInfo(
                null,
                null,
                0,
                null,
                null,
                null,
                false
        );

        assertNull(info.idPolitica());
        assertNull(info.nombre());
        assertEquals(0, info.version());
        assertNull(info.descripcion());
        assertNull(info.anticipacionHoras());
        assertNull(info.porcentajeDevolucion());
        assertFalse(info.activa());
    }

    @Test
    void debeConservarEstadoActivo() {
        PoliticaCancelacionInfo info = new PoliticaCancelacionInfo(
                UUID.randomUUID(),
                "Flexible",
                1,
                "Descripción.",
                48,
                new BigDecimal("100.00"),
                true
        );

        assertTrue(info.activa());
    }

    @Test
    void debeConservarEstadoInactivo() {
        PoliticaCancelacionInfo info = new PoliticaCancelacionInfo(
                UUID.randomUUID(),
                "Flexible",
                1,
                "Descripción.",
                48,
                new BigDecimal("100.00"),
                false
        );

        assertFalse(info.activa());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        UUID idPolitica = UUID.randomUUID();

        PoliticaCancelacionInfo info = new PoliticaCancelacionInfo(
                idPolitica,
                "Flexible",
                2,
                "Descripción de prueba.",
                48,
                new BigDecimal("75.50"),
                true
        );

        String resultado = info.toString();

        assertTrue(resultado.contains(idPolitica.toString()));
        assertTrue(resultado.contains("Flexible"));
        assertTrue(resultado.contains("75.50"));
        assertTrue(resultado.contains("true"));
    }
}
