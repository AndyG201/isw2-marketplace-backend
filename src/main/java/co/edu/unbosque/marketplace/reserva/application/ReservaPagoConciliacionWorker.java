package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;

@Component
public class ReservaPagoConciliacionWorker {

    private final RegistroPagos registro;
    private final ReservaPagoSimuladoService pagos;

    public ReservaPagoConciliacionWorker(
            RegistroPagos registro,
            ReservaPagoSimuladoService pagos) {
        this.registro = registro;
        this.pagos = pagos;
    }

    public void conciliarPendientes() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        registro.clavesPorConciliar(ahora).forEach(pagos::conciliar);
    }
}
