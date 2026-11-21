package co.edu.unbosque.marketplace.cotizacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CotizacionTest {

    @Test
    void constructorVacio() {
        Cotizacion cotizacion = new Cotizacion();

        assertNotNull(cotizacion);
        assertNull(cotizacion.getIdCotizacion());
        assertNull(cotizacion.getIdPublicacion());
        assertNull(cotizacion.getIdConfiguracion());
        assertNull(cotizacion.getFechaEntrada());
        assertNull(cotizacion.getFechaSalida());
        assertEquals(0, cotizacion.getCantidadHuespedes());
        assertNull(cotizacion.getPrecioNocheAplicado());
        assertNull(cotizacion.getTarifaLimpieza());
        assertNull(cotizacion.getFechaCotizacion());
    }

    @Test
    void constructorCompleto() {
        UUID idCotizacion = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        UUID idConfiguracion = UUID.randomUUID();
        LocalDate fechaEntrada = LocalDate.of(2026, 12, 10);
        LocalDate fechaSalida = LocalDate.of(2026, 12, 15);
        BigDecimal precioNoche = new BigDecimal("150000.00");
        BigDecimal tarifaLimpieza = new BigDecimal("50000.00");
        OffsetDateTime fechaCotizacion = OffsetDateTime.now();

        Cotizacion cotizacion = new Cotizacion(
                idCotizacion,
                idPublicacion,
                idConfiguracion,
                fechaEntrada,
                fechaSalida,
                3,
                precioNoche,
                tarifaLimpieza,
                fechaCotizacion
        );

        assertEquals(idCotizacion, cotizacion.getIdCotizacion());
        assertEquals(idPublicacion, cotizacion.getIdPublicacion());
        assertEquals(idConfiguracion, cotizacion.getIdConfiguracion());
        assertEquals(fechaEntrada, cotizacion.getFechaEntrada());
        assertEquals(fechaSalida, cotizacion.getFechaSalida());
        assertEquals(3, cotizacion.getCantidadHuespedes());
        assertEquals(precioNoche, cotizacion.getPrecioNocheAplicado());
        assertEquals(tarifaLimpieza, cotizacion.getTarifaLimpieza());
        assertEquals(fechaCotizacion, cotizacion.getFechaCotizacion());
    }

    @Test
    void gettersRetornanLosValoresAsignados() {
        Cotizacion cotizacion = crearCotizacion();

        assertNotNull(cotizacion.getIdCotizacion());
        assertNotNull(cotizacion.getIdPublicacion());
        assertNotNull(cotizacion.getIdConfiguracion());
        assertEquals(LocalDate.of(2026, 12, 10), cotizacion.getFechaEntrada());
        assertEquals(LocalDate.of(2026, 12, 15), cotizacion.getFechaSalida());
        assertEquals(2, cotizacion.getCantidadHuespedes());
        assertEquals(new BigDecimal("150000.00"), cotizacion.getPrecioNocheAplicado());
        assertEquals(new BigDecimal("50000.00"), cotizacion.getTarifaLimpieza());
        assertNotNull(cotizacion.getFechaCotizacion());
    }

    @Test
    void settersActualizanLosAtributos() {
        Cotizacion cotizacion = new Cotizacion();

        UUID idCotizacion = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        UUID idConfiguracion = UUID.randomUUID();
        LocalDate fechaEntrada = LocalDate.of(2027, 1, 10);
        LocalDate fechaSalida = LocalDate.of(2027, 1, 15);
        BigDecimal precioNoche = new BigDecimal("200000.00");
        BigDecimal tarifaLimpieza = new BigDecimal("60000.00");
        OffsetDateTime fechaCotizacion = OffsetDateTime.now();

        cotizacion.setIdCotizacion(idCotizacion);
        cotizacion.setIdPublicacion(idPublicacion);
        cotizacion.setIdConfiguracion(idConfiguracion);
        cotizacion.setFechaEntrada(fechaEntrada);
        cotizacion.setFechaSalida(fechaSalida);
        cotizacion.setCantidadHuespedes(4);
        cotizacion.setPrecioNocheAplicado(precioNoche);
        cotizacion.setTarifaLimpieza(tarifaLimpieza);
        cotizacion.setFechaCotizacion(fechaCotizacion);

        assertEquals(idCotizacion, cotizacion.getIdCotizacion());
        assertEquals(idPublicacion, cotizacion.getIdPublicacion());
        assertEquals(idConfiguracion, cotizacion.getIdConfiguracion());
        assertEquals(fechaEntrada, cotizacion.getFechaEntrada());
        assertEquals(fechaSalida, cotizacion.getFechaSalida());
        assertEquals(4, cotizacion.getCantidadHuespedes());
        assertEquals(precioNoche, cotizacion.getPrecioNocheAplicado());
        assertEquals(tarifaLimpieza, cotizacion.getTarifaLimpieza());
        assertEquals(fechaCotizacion, cotizacion.getFechaCotizacion());
    }

    @Test
    void settersPermitenValoresNulosEnAtributosDeReferencia() {
        Cotizacion cotizacion = crearCotizacion();

        cotizacion.setIdCotizacion(null);
        cotizacion.setIdPublicacion(null);
        cotizacion.setIdConfiguracion(null);
        cotizacion.setFechaEntrada(null);
        cotizacion.setFechaSalida(null);
        cotizacion.setPrecioNocheAplicado(null);
        cotizacion.setTarifaLimpieza(null);
        cotizacion.setFechaCotizacion(null);

        assertNull(cotizacion.getIdCotizacion());
        assertNull(cotizacion.getIdPublicacion());
        assertNull(cotizacion.getIdConfiguracion());
        assertNull(cotizacion.getFechaEntrada());
        assertNull(cotizacion.getFechaSalida());
        assertNull(cotizacion.getPrecioNocheAplicado());
        assertNull(cotizacion.getTarifaLimpieza());
        assertNull(cotizacion.getFechaCotizacion());
    }

    private Cotizacion crearCotizacion() {
        return new Cotizacion(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                2,
                new BigDecimal("150000.00"),
                new BigDecimal("50000.00"),
                OffsetDateTime.now()
        );
    }
}
