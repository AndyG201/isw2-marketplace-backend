package co.edu.unbosque.marketplace.reserva.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionInfo;

public interface ReservaCancelacionRepository {

    Optional<ReservaCancelacionInfo> bloquearYConsultar(UUID idReserva);

    void marcarCancelada(UUID idReserva);
}
