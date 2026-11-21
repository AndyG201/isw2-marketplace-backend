package co.edu.unbosque.marketplace.reserva.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaInfo;

public interface ReservaResenaQueryRepository {

    Optional<ReservaResenaInfo> consultar(UUID idReserva);
}
