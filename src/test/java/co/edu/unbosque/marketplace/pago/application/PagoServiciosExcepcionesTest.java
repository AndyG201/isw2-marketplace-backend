package co.edu.unbosque.marketplace.pago.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.pago.application.port.OperacionPendienteRepository;
import co.edu.unbosque.marketplace.pago.application.port.PagoRepository;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsoPagoRepository;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsosPendientes;
import co.edu.unbosque.marketplace.pago.domain.Pago;
import co.edu.unbosque.marketplace.pago.domain.ReembolsoPago;

class PagoServiciosExcepcionesTest {

    private final PagoRepository pagos = mock(PagoRepository.class);
    private final ReembolsoPagoRepository reembolsos = mock(ReembolsoPagoRepository.class);
    private final OffsetDateTime ahora = OffsetDateTime.now();
    private final UUID idReembolso = UUID.randomUUID();

    // ---------- ReembolsoPagoService ----------

    private ReembolsoPagoService servicioReembolsos() {
        return new ReembolsoPagoService(mock(ReembolsosPendientes.class), reembolsos, pagos);
    }

    @Test
    void confirmarFallaSiNoExisteElReembolso() {
        when(reembolsos.findById(idReembolso)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicioReembolsos().confirmar(idReembolso, "re_1", ahora));
    }

    @Test
    void confirmarFallaSiNoExisteElPagoAsociado() {
        UUID idPago = UUID.randomUUID();
        ReembolsoPago reembolso = new ReembolsoPago(idReembolso, idPago, UUID.randomUUID(), "pi_1", "refund-1", ahora);
        when(reembolsos.findById(idReembolso)).thenReturn(Optional.of(reembolso));
        when(pagos.findById(idPago)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicioReembolsos().confirmar(idReembolso, "re_1", ahora));
    }

    @Test
    void reprogramarYMarcarFallaFallanSiNoExisteElReembolso() {
        when(reembolsos.findById(idReembolso)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicioReembolsos().reprogramar(idReembolso, null, "error", ahora));
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicioReembolsos().marcarFallaDefinitiva(idReembolso, null, "error", ahora));
    }

    // ---------- RegistroPagosService ----------

    private RegistroPagosService registroPagos() {
        return new RegistroPagosService(pagos, mock(OperacionPendienteRepository.class), reembolsos);
    }

    @Test
    void programarReembolsoFallaSiNoExisteElPago() {
        when(pagos.findByClaveIdempotencia("k")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> registroPagos().programarReembolso("k", ahora));
    }

    @Test
    void programarReembolsoSoloAplicaAPagosCapturados() {
        Pago pago = new Pago();
        pago.setEstado("PENDIENTE");
        when(pagos.findByClaveIdempotencia("k")).thenReturn(Optional.of(pago));

        assertThrows(ConflictoNegocioException.class, () -> registroPagos().programarReembolso("k", ahora));
    }

    @Test
    void programarReembolsoExigeLaReferenciaDeStripe() {
        Pago pago = new Pago();
        pago.setEstado("PAGADO");
        pago.setReferenciaStripe(" ");
        when(pagos.findByClaveIdempotencia("k")).thenReturn(Optional.of(pago));

        assertThrows(FallaInternaException.class, () -> registroPagos().programarReembolso("k", ahora));
    }
}
