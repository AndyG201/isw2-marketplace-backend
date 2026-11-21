package co.edu.unbosque.marketplace.reserva.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

public interface ReservaRetencionRepository extends RepositorioBasico<Reserva, UUID> {
    Optional<Reserva> bloquearPorId(UUID id);
    List<UUID> buscarRetencionesVencidas(OffsetDateTime ahora);
}
