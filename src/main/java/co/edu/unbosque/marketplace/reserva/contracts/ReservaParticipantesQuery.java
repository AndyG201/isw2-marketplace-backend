package co.edu.unbosque.marketplace.reserva.contracts;

import java.util.Optional;
import java.util.UUID;

public interface ReservaParticipantesQuery {

    Optional<ReservaParticipantes> consultarParticipantes(UUID idReserva);
}
