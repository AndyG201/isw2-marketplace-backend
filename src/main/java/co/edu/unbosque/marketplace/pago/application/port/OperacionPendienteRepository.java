package co.edu.unbosque.marketplace.pago.application.port;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.pago.domain.OperacionPendiente;

public interface OperacionPendienteRepository extends RepositorioBasico<OperacionPendiente, UUID> {
    Optional<OperacionPendiente> findByClaveIdempotencia(String claveIdempotencia);
    Optional<OperacionPendiente> findFirstByIdReservaAndEstadoIn(UUID idReserva, Collection<String> estados);
    List<OperacionPendiente> findTop25ByEstadoAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(String estado, OffsetDateTime fecha);
}
