package co.edu.unbosque.marketplace.pago.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.pago.application.port.OperacionPendienteRepository;
import co.edu.unbosque.marketplace.pago.application.port.PagoRepository;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsoPagoRepository;
import co.edu.unbosque.marketplace.pago.domain.Pago;
import co.edu.unbosque.marketplace.pago.domain.ReembolsoPago;

@ExtendWith(MockitoExtension.class)
class RegistroPagosServiceReembolsoTest {

    @Mock private PagoRepository pagos;
    @Mock private OperacionPendienteRepository operaciones;
    @Mock private ReembolsoPagoRepository reembolsos;
    @InjectMocks private RegistroPagosService servicio;

    @Test
    void programaUnaObligacionPersistenteParaPagoAprobado() {
        UUID idPago = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();
        Pago pago = new Pago(idPago, idReserva, "pi_123", "clave-pago",
                new BigDecimal("125000.00"), "PAGADO", ahora, ahora);
        when(pagos.findByClaveIdempotencia("clave-pago")).thenReturn(Optional.of(pago));
        when(reembolsos.findByIdPago(idPago)).thenReturn(Optional.empty());

        servicio.programarReembolso("clave-pago", ahora);

        ArgumentCaptor<ReembolsoPago> captor = ArgumentCaptor.forClass(ReembolsoPago.class);
        verify(reembolsos).saveAndFlush(captor.capture());
        assertEquals(idPago, captor.getValue().getIdPago());
        assertEquals("pi_123", captor.getValue().getPaymentIntentId());
        assertEquals("PENDIENTE", captor.getValue().getEstado());
        assertEquals("refund-" + idPago, captor.getValue().getClaveIdempotencia());
    }

    @Test
    void noDuplicaLaObligacionSiYaExisteParaElPago() {
        UUID idPago = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();
        Pago pago = new Pago(idPago, UUID.randomUUID(), "pi_123", "clave-pago",
                new BigDecimal("125000.00"), "PAGADO", ahora, ahora);
        when(pagos.findByClaveIdempotencia("clave-pago")).thenReturn(Optional.of(pago));
        when(reembolsos.findByIdPago(idPago)).thenReturn(Optional.of(mock(ReembolsoPago.class)));

        servicio.programarReembolso("clave-pago", ahora);

        verify(reembolsos, never()).saveAndFlush(any());
    }
}
