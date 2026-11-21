package co.edu.unbosque.marketplace.reserva.contracts;

import java.util.Optional;
import java.util.UUID;

public interface ReservaCancelacionGateway {

    Optional<ReservaCancelacionInfo> bloquearYConsultar(UUID idReserva);

    void cancelarYLiberar(UUID idReserva);
}
