package co.edu.unbosque.marketplace.reporte.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class FormatoReporteTest {

    @Test
    void laFechaSeEscribeEnEspanolYEnHoraDeColombia() {
        // 16:24 UTC son las 11:24 de la mañana en Bogotá.
        assertEquals("22 de noviembre de 2026, 11:24 a. m.",
                FormatoReporte.fechaHora(OffsetDateTime.parse("2026-11-22T16:24:00Z")));
        assertEquals("1 de enero de 2027, 12:05 p. m.",
                FormatoReporte.fechaHora(OffsetDateTime.parse("2027-01-01T17:05:00Z")));
        assertEquals("3 de marzo de 2027, 12:00 a. m.",
                FormatoReporte.fechaHora(OffsetDateTime.parse("2027-03-03T05:00:00Z")));
    }

    @Test
    void elDineroLlevaPuntosDeMilesYNingunDecimal() {
        assertEquals("$ 0", FormatoReporte.moneda(BigDecimal.ZERO));
        assertEquals("$ 273.998", FormatoReporte.moneda(new BigDecimal("273998.00")));
        assertEquals("$ 1.250.000", FormatoReporte.moneda(new BigDecimal("1250000")));
        assertEquals("$ 1.001", FormatoReporte.moneda(new BigDecimal("1000.50")));
        assertEquals("-$ 10.000", FormatoReporte.moneda(new BigDecimal("-10000")));
    }

    @Test
    void losEnterosLlevanPuntosDeMiles() {
        assertEquals("45", FormatoReporte.entero(45));
        assertEquals("12.345", FormatoReporte.entero(12345));
    }

    @Test
    void elPorcentajeUsaComaYNoFallaConTotalCero() {
        assertEquals("12,5 %", FormatoReporte.porcentaje(1, 8));
        assertEquals("55,6 %", FormatoReporte.porcentaje(25, 45));
        assertEquals("100 %", FormatoReporte.porcentaje(3, 3));
        assertEquals("0 %", FormatoReporte.porcentaje(0, 10));
        assertEquals("0 %", FormatoReporte.porcentaje(5, 0));
    }

    @Test
    void losEstadosSeLeenComoEnLaAplicacion() {
        assertEquals("Pendiente de aprobación", FormatoReporte.etiquetaEstado("PENDIENTE_APROBACION"));
        assertEquals("Pendiente de pago", FormatoReporte.etiquetaEstado("PENDIENTE_PAGO"));
        assertEquals("Completada", FormatoReporte.etiquetaEstado("COMPLETADA"));
        assertEquals("En revision", FormatoReporte.etiquetaEstado("EN_REVISION"));
    }

    @Test
    void losEstadosSalenEnElOrdenDelCicloYNoSePierdeNingunoDesconocido() {
        Map<String, Long> reservas = new LinkedHashMap<>();
        reservas.put("ZETA", 1L);
        reservas.put("CANCELADA", 2L);
        reservas.put("PENDIENTE_APROBACION", 3L);
        reservas.put("ALFA", 4L);

        assertEquals(List.of("PENDIENTE_APROBACION", "CANCELADA", "ALFA", "ZETA"),
                FormatoReporte.estadosOrdenados(reservas));
        assertEquals(10, FormatoReporte.total(reservas));
    }
}
