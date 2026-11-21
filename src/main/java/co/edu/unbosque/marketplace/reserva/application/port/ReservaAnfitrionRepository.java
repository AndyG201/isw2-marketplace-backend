package co.edu.unbosque.marketplace.reserva.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaAnfitrionRepository {

    List<ReservaAnfitrionDatos> consultarPorAnfitrion(UUID idAnfitrion);

    Optional<ReservaAnfitrionDatos> consultarReservaPorAnfitrion(UUID idAnfitrion, UUID idReserva);
}
