package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.pago.application.port.OperacionPendienteRepository;

@Component
public class ReservaPagoConciliacionWorker {

    private final OperacionPendienteRepository operaciones;
    private final ReservaPagoSimuladoService pagos;

    public ReservaPagoConciliacionWorker(
            OperacionPendienteRepository operaciones,
            ReservaPagoSimuladoService pagos) {
        this.operaciones = operaciones;
        this.pagos = pagos;
    }

    public void conciliarPendientes() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        operaciones.findTop25ByEstadoAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(
                        "PENDIENTE", ahora)
                .forEach(operacion -> pagos.conciliar(operacion.getClaveIdempotencia()));
    }
}
