package co.edu.unbosque.marketplace.reserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.pago.contracts.PagoInfo;
import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;
import co.edu.unbosque.marketplace.reserva.application.port.BloqueoClaveIdempotencia;
import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaConfirmacionRepository;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@ExtendWith(MockitoExtension.class)
class ReservaPagoIntentoReembolsoTest {

    @Mock private ReservaConfirmacionRepository reservas;
    @Mock private RegistroPagos pagos;
    @Mock private BloqueoClaveIdempotencia bloqueo;
    @Mock private NocheReservaRepository noches;
    @InjectMocks private ReservaPagoIntentoService servicio;

    @Test
    void pagoAprobadoDespuesDelVencimientoProgramaReembolsoYExpiraReserva() {
        UUID idReserva = UUID.randomUUID();
        UUID idPago = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();
        Reserva reserva = new Reserva();
        reserva.setIdReserva(idReserva);
        reserva.setEstado("PENDIENTE_PAGO");
        reserva.setRetencionHasta(ahora.minusMinutes(1));
        PagoInfo previo = new PagoInfo(idPago, idReserva, null, "clave", new BigDecimal("100.00"),
                "PENDIENTE", ahora.minusMinutes(10), null);
        PagoInfo aprobado = new PagoInfo(idPago, idReserva, "pi_123", "clave", new BigDecimal("100.00"),
                "PAGADO", ahora.minusMinutes(10), ahora);
        when(pagos.buscarPorClave("clave")).thenReturn(Optional.of(previo));
        when(reservas.bloquearPorId(idReserva)).thenReturn(Optional.of(reserva));
        when(pagos.registrarResultado(eq("clave"), eq(true), eq("pi_123"), anyString(), any()))
                .thenReturn(aprobado);

        servicio.registrarResultado("clave", true, "pi_123", "aprobado");

        assertEquals("EXPIRADA", reserva.getEstado());
        assertEquals(null, reserva.getRetencionHasta());
        verify(pagos).programarReembolso(eq("clave"), any(OffsetDateTime.class));
        verify(noches).liberar(idReserva);
        verify(reservas).save(reserva);
    }

    @Test
    void pagoAprobadoConRetencionVigenteConfirmaSinReembolso() {
        UUID idReserva = UUID.randomUUID();
        UUID idPago = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();
        Reserva reserva = new Reserva();
        reserva.setIdReserva(idReserva);
        reserva.setEstado("PENDIENTE_PAGO");
        reserva.setRetencionHasta(ahora.plusMinutes(10));
        PagoInfo previo = new PagoInfo(idPago, idReserva, null, "clave", new BigDecimal("100.00"),
                "PENDIENTE", ahora.minusMinutes(10), null);
        PagoInfo aprobado = new PagoInfo(idPago, idReserva, "pi_123", "clave", new BigDecimal("100.00"),
                "PAGADO", ahora.minusMinutes(10), ahora);
        when(pagos.buscarPorClave("clave")).thenReturn(Optional.of(previo));
        when(reservas.bloquearPorId(idReserva)).thenReturn(Optional.of(reserva));
        when(pagos.registrarResultado(eq("clave"), eq(true), eq("pi_123"), anyString(), any()))
                .thenReturn(aprobado);

        servicio.registrarResultado("clave", true, "pi_123", "aprobado");

        assertEquals("CONFIRMADA", reserva.getEstado());
        verify(pagos, never()).programarReembolso(anyString(), any());
        verify(noches, never()).liberar(any());
    }
}
