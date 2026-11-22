package co.edu.unbosque.marketplace.pago.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import co.edu.unbosque.marketplace.pago.application.port.PagoRepository;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsoPagoRepository;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsosPendientes;
import co.edu.unbosque.marketplace.pago.domain.Pago;
import co.edu.unbosque.marketplace.pago.domain.ReembolsoPago;

/** Ciclo de vida de un reembolso: reclamo, confirmación, reintentos con espera creciente y falla definitiva. */
class ReembolsoPagoServiceTest {

    private final ReembolsosPendientes pendientes = mock(ReembolsosPendientes.class);
    private final ReembolsoPagoRepository reembolsos = mock(ReembolsoPagoRepository.class);
    private final PagoRepository pagos = mock(PagoRepository.class);
    private final ReembolsoPagoService servicio = new ReembolsoPagoService(pendientes, reembolsos, pagos);

    private final OffsetDateTime ahora = OffsetDateTime.parse("2030-01-01T10:00:00Z");
    private final UUID idReembolso = UUID.randomUUID();
    private final UUID idPago = UUID.randomUUID();

    private ReembolsoPago reembolso(int intentos) {
        ReembolsoPago r = new ReembolsoPago(idReembolso, idPago, UUID.randomUUID(), "pi_1", "refund-1", ahora.minusHours(1));
        r.setIntentos(intentos);
        r.setEstado("EN_PROCESO");
        when(reembolsos.findById(idReembolso)).thenReturn(Optional.of(r));
        return r;
    }

    @Test
    void reclamarPendientesDelegaEnElPuerto() {
        List<ReembolsoTrabajo> trabajos = List.of(
                new ReembolsoTrabajo(idReembolso, "pi_1", null, "refund-1", new BigDecimal("100.00")));
        when(pendientes.reclamar(ahora)).thenReturn(trabajos);

        assertSame(trabajos, servicio.reclamarPendientes(ahora));
    }

    @Test
    void confirmarMarcaElReembolsoYElPagoComoReembolsados() {
        ReembolsoPago r = reembolso(1);
        r.setUltimoError("error previo");
        Pago pago = new Pago();
        pago.setEstado("PAGADO");
        when(pagos.findById(idPago)).thenReturn(Optional.of(pago));

        servicio.confirmar(idReembolso, "re_1", ahora);

        assertEquals("REEMBOLSADO", r.getEstado());
        assertEquals("re_1", r.getStripeRefundId());
        assertNull(r.getUltimoError());
        assertEquals(ahora, r.getFechaActualizacion());
        assertEquals("REEMBOLSADO", pago.getEstado());
        verify(reembolsos).save(r);
        verify(pagos).save(pago);
    }

    @Test
    void confirmarUnReembolsoYaConfirmadoNoHaceNada() {
        ReembolsoPago r = reembolso(1);
        r.setEstado("REEMBOLSADO");

        servicio.confirmar(idReembolso, "re_1", ahora);

        verify(reembolsos, never()).save(r);
        verify(pagos, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @ParameterizedTest
    @CsvSource({"1,30", "2,60", "3,120", "4,240", "5,480", "6,960", "7,1920", "8,3600", "11,3600"})
    void reprogramarUsaUnaEsperaCrecienteConTope(int intentos, long segundosEsperados) {
        ReembolsoPago r = reembolso(intentos);

        servicio.reprogramar(idReembolso, "re_1", "Stripe mantiene el reembolso pendiente.", ahora);

        assertEquals("PENDIENTE", r.getEstado());
        assertEquals(ahora.plusSeconds(segundosEsperados), r.getProximaEjecucion());
        assertEquals("Stripe mantiene el reembolso pendiente.", r.getUltimoError());
        assertEquals("re_1", r.getStripeRefundId());
        verify(reembolsos).save(r);
    }

    @Test
    void alAlcanzarElMaximoDeIntentosElReembolsoFalla() {
        ReembolsoPago r = reembolso(12);

        servicio.reprogramar(idReembolso, null, "sin respuesta", ahora);

        assertEquals("FALLIDA", r.getEstado());
        assertEquals("sin respuesta", r.getUltimoError());
    }

    @Test
    void elMensajeDeErrorSeLimitaYTieneUnValorPorDefecto() {
        ReembolsoPago largo = reembolso(1);
        servicio.reprogramar(idReembolso, null, "x".repeat(2500), ahora);
        assertEquals(2000, largo.getUltimoError().length());

        ReembolsoPago vacio = reembolso(1);
        servicio.reprogramar(idReembolso, null, "  ", ahora);
        assertEquals("Stripe no confirmó el reembolso.", vacio.getUltimoError());

        ReembolsoPago nulo = reembolso(1);
        servicio.marcarFallaDefinitiva(idReembolso, null, null, ahora);
        assertEquals("Stripe no confirmó el reembolso.", nulo.getUltimoError());
    }

    @Test
    void marcarFallaDefinitivaCierraElReembolso() {
        ReembolsoPago r = reembolso(3);

        servicio.marcarFallaDefinitiva(idReembolso, "re_9", "Stripe rechazó la devolución.", ahora);

        assertEquals("FALLIDA", r.getEstado());
        assertEquals("re_9", r.getStripeRefundId());
        assertEquals("Stripe rechazó la devolución.", r.getUltimoError());
        assertEquals(ahora, r.getFechaActualizacion());
        verify(reembolsos).save(r);
    }
}
