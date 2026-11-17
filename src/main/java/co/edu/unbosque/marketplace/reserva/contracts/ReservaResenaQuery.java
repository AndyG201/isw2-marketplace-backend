package co.edu.unbosque.marketplace.reserva.contracts;

import java.util.Optional;
import java.util.UUID;

public interface ReservaResenaQuery {

    Optional<ReservaResenaInfo> consultarReservaParaResena(UUID idReserva);
}
