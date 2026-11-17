package co.edu.unbosque.marketplace.reserva.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionGateway;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionInfo;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.NocheReservaJdbcRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaCancelacionJdbcRepository;

@Service
public class ReservaCancelacionGatewayService implements ReservaCancelacionGateway {

    private final ReservaCancelacionJdbcRepository reservas;
    private final NocheReservaJdbcRepository noches;

    public ReservaCancelacionGatewayService(
            ReservaCancelacionJdbcRepository reservas,
            NocheReservaJdbcRepository noches) {
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
