package co.edu.unbosque.marketplace.pago.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.pago.domain.OperacionPendiente;

@Repository
public interface OperacionPendienteRepository extends JpaRepository<OperacionPendiente, UUID> {

    Optional<OperacionPendiente> findByClaveIdempotencia(String claveIdempotencia);

    Optional<OperacionPendiente> findFirstByIdReservaAndEstadoIn(
            UUID idReserva,
            Collection<String> estados);

    List<OperacionPendiente> findTop25ByEstadoAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(
            String estado,
            OffsetDateTime fecha);
}
