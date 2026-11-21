package co.edu.unbosque.marketplace.pago.application;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoReembolsoSandbox;

@ExtendWith(MockitoExtension.class)
class ReembolsoPagoWorkerTest {

    @Mock private ReembolsoPagoService servicio;
    @Mock private PagoSandboxGateway stripe;
    @InjectMocks private ReembolsoPagoWorker worker;

    @Test
    void confirmaSoloCuandoStripeDevuelveSucceeded() {
        UUID id = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();
        ReembolsoTrabajo trabajo = new ReembolsoTrabajo(
                id, "pi_123", null, "refund-123", new BigDecimal("100.00"));
        when(servicio.reclamarPendientes(any())).thenReturn(List.of(trabajo));
        when(stripe.reembolsar("pi_123", new BigDecimal("100.00"), "refund-123"))
                .thenReturn(new ResultadoReembolsoSandbox("re_123", "succeeded", "ok"));

        worker.procesarPendientes();

        verify(servicio).confirmar(eq(id), eq("re_123"), any(OffsetDateTime.class));
        verify(servicio, never()).reprogramar(any(), any(), any(), any());
    }

    @Test
    void conservaReferenciaYReprogramaCuandoStripeMantieneElReembolsoPendiente() {
        UUID id = UUID.randomUUID();
        ReembolsoTrabajo trabajo = new ReembolsoTrabajo(
                id, "pi_123", null, "refund-123", new BigDecimal("100.00"));
        when(servicio.reclamarPendientes(any())).thenReturn(List.of(trabajo));
        when(stripe.reembolsar(anyString(), any(), anyString()))
                .thenReturn(new ResultadoReembolsoSandbox("re_123", "pending", "pending"));

        worker.procesarPendientes();

        verify(servicio).reprogramar(eq(id), eq("re_123"), anyString(), any(OffsetDateTime.class));
        verify(servicio, never()).confirmar(any(), any(), any());
    }

    @Test
    void reintentaConLaMismaClaveCuandoStripeLanzaErrorTransitorio() {
        UUID id = UUID.randomUUID();
        ReembolsoTrabajo trabajo = new ReembolsoTrabajo(
                id, "pi_123", null, "refund-123", new BigDecimal("100.00"));
        when(servicio.reclamarPendientes(any())).thenReturn(List.of(trabajo));
        when(stripe.reembolsar(anyString(), any(), anyString()))
                .thenThrow(new IllegalStateException("timeout"));

        worker.procesarPendientes();

        verify(stripe).reembolsar("pi_123", new BigDecimal("100.00"), "refund-123");
        verify(servicio).reprogramar(eq(id), isNull(), eq("timeout"), any(OffsetDateTime.class));
    }
}
