package co.edu.unbosque.marketplace.reserva.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservaHistorialRepository {

    List<ReservaHistorialDatos> consultarPorHuesped(UUID idHuesped);

    Optional<ReservaHistorialDatos> consultarPorId(UUID idReserva);
}
