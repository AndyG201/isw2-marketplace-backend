package co.edu.unbosque.marketplace.reserva.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionGateway;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionInfo;
import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaCancelacionRepository;

@Service
public class ReservaCancelacionGatewayService implements ReservaCancelacionGateway {

    private final ReservaCancelacionRepository reservas;
    private final NocheReservaRepository noches;

    public ReservaCancelacionGatewayService(
            ReservaCancelacionRepository reservas,
            NocheReservaRepository noches) {
        this.reservas = reservas;
        this.noches = noches;
    }

    @Override
    @Transactional
    public Optional<ReservaCancelacionInfo> bloquearYConsultar(UUID idReserva) {
        return reservas.bloquearYConsultar(idReserva);
    }

    @Override
    @Transactional
    public void cancelarYLiberar(UUID idReserva) {
        reservas.marcarCancelada(idReserva);
        noches.liberar(idReserva);
    }
}
