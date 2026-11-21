package co.edu.unbosque.marketplace.reserva.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;
import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaCancelacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionGateway;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionInfo;

@Service
public class ReservaCancelacionGatewayService implements ReservaCancelacionGateway {

    private final ReservaCancelacionRepository reservas;
    private final NocheReservaRepository noches;
    private final RegistroPagos pagos;

    public ReservaCancelacionGatewayService(
            ReservaCancelacionRepository reservas,
            NocheReservaRepository noches,
            RegistroPagos pagos) {
        this.reservas = reservas;
        this.noches = noches;
        this.pagos = pagos;
    }

    @Override
    @Transactional
    public Optional<ReservaCancelacionInfo> bloquearYConsultar(UUID idReserva) {
        return reservas.bloquearYConsultar(idReserva)
                .map(reserva -> pagos.ultimoDeReserva(idReserva)
                        .map(pago -> reserva.conPago(pago.monto(), pago.estado()))
                        .orElse(reserva));
    }

    @Override
    @Transactional
    public void cancelarYLiberar(UUID idReserva) {
        reservas.marcarCancelada(idReserva);
        noches.liberar(idReserva);
    }
}
