package co.edu.unbosque.marketplace.pago.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PagoTest {

    @Test
    void constructorVacioDebeCrearObjeto() {
        Pago pago = new Pago();

        assertNotNull(pago);
        assertNull(pago.getIdPago());
        assertNull(pago.getIdReserva());
        assertNull(pago.getReferenciaStripe());
        assertNull(pago.getClaveIdempotencia());
        assertNull(pago.getMonto());
        assertNull(pago.getEstado());
        assertNull(pago.getFechaCreacion());
        assertNull(pago.getFechaPago());
    }

    @Test
    void constructorCompletoDebeAsignarTodosLosAtributos() {
        UUID idPago = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("250000.50");
        OffsetDateTime fechaCreacion = OffsetDateTime.now();
        OffsetDateTime fechaPago = fechaCreacion.plusMinutes(10);

        Pago pago = new Pago(
                idPago,
                idReserva,
                "pi_stripe_123",
                "clave-idempotencia-123",
                monto,
                "APROBADO",
                fechaCreacion,
                fechaPago
        );

        assertEquals(idPago, pago.getIdPago());
        assertEquals(idReserva, pago.getIdReserva());
        assertEquals("pi_stripe_123", pago.getReferenciaStripe());
        assertEquals("clave-idempotencia-123", pago.getClaveIdempotencia());
        assertEquals(0, monto.compareTo(pago.getMonto()));
        assertEquals("APROBADO", pago.getEstado());
        assertEquals(fechaCreacion, pago.getFechaCreacion());
        assertEquals(fechaPago, pago.getFechaPago());
    }

    @Test
    void settersDebenActualizarTodosLosAtributos() {
        Pago pago = new Pago();

        UUID idPago = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("180000.75");
        OffsetDateTime fechaCreacion = OffsetDateTime.now();
        OffsetDateTime fechaPago = fechaCreacion.plusHours(1);

        pago.setIdPago(idPago);
        pago.setIdReserva(idReserva);
        pago.setReferenciaStripe("pi_stripe_456");
        pago.setClaveIdempotencia("clave-456");
        pago.setMonto(monto);
        pago.setEstado("PENDIENTE");
        pago.setFechaCreacion(fechaCreacion);
        pago.setFechaPago(fechaPago);

        assertEquals(idPago, pago.getIdPago());
        assertEquals(idReserva, pago.getIdReserva());
        assertEquals("pi_stripe_456", pago.getReferenciaStripe());
        assertEquals("clave-456", pago.getClaveIdempotencia());
        assertEquals(0, monto.compareTo(pago.getMonto()));
        assertEquals("PENDIENTE", pago.getEstado());
        assertEquals(fechaCreacion, pago.getFechaCreacion());
        assertEquals(fechaPago, pago.getFechaPago());
    }

    @Test
    void atributosOpcionalesPuedenSerNulos() {
        UUID idPago = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        OffsetDateTime fechaCreacion = OffsetDateTime.now();

        Pago pago = new Pago(
                idPago,
                idReserva,
                null,
                "clave-789",
                new BigDecimal("100000.00"),
                "PENDIENTE",
                fechaCreacion,
                null
        );

        assertNull(pago.getReferenciaStripe());
        assertNull(pago.getFechaPago());
    }

    @Test
    void debePermitirActualizarElMonto() {
        Pago pago = new Pago();

        pago.setMonto(new BigDecimal("50000.00"));
        assertEquals(0, new BigDecimal("50000.00").compareTo(pago.getMonto()));

        pago.setMonto(new BigDecimal("125000.50"));
        assertEquals(0, new BigDecimal("125000.50").compareTo(pago.getMonto()));
    }

    @Test
    void debeConservarLaPrecisionDelMonto() {
        BigDecimal monto = new BigDecimal("123456.7890");

        Pago pago = new Pago();
        pago.setMonto(monto);

        assertEquals(monto, pago.getMonto());
        assertEquals(4, pago.getMonto().scale());
    }

    @Test
    void debeConservarLosOffsetsDeLasFechas() {
        OffsetDateTime fechaCreacion =
                OffsetDateTime.parse("2026-11-21T10:30:00-05:00");

        OffsetDateTime fechaPago =
                OffsetDateTime.parse("2026-11-21T10:45:00-05:00");

        Pago pago = new Pago();
        pago.setFechaCreacion(fechaCreacion);
        pago.setFechaPago(fechaPago);

        assertEquals(fechaCreacion, pago.getFechaCreacion());
        assertEquals(fechaPago, pago.getFechaPago());
        assertEquals("-05:00", pago.getFechaCreacion().getOffset().toString());
        assertEquals("-05:00", pago.getFechaPago().getOffset().toString());
    }

    @Test
    void debePermitirCambiarElEstadoDelPago() {
        Pago pago = new Pago();

        pago.setEstado("PENDIENTE");
        assertEquals("PENDIENTE", pago.getEstado());

        pago.setEstado("APROBADO");
        assertEquals("APROBADO", pago.getEstado());

        pago.setEstado("RECHAZADO");
        assertEquals("RECHAZADO", pago.getEstado());
    }
}
