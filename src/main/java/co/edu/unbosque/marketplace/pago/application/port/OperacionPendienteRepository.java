package co.edu.unbosque.marketplace.pago.application.port;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.pago.domain.OperacionPendiente;

public interface OperacionPendienteRepository {

    <S extends OperacionPendiente> S save(S entidad);
    <S extends OperacionPendiente> List<S> saveAll(Iterable<S> entidades);
    <S extends OperacionPendiente> S saveAndFlush(S entidad);
    Optional<OperacionPendiente> findById(UUID id);
    List<OperacionPendiente> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(OperacionPendiente entidad);
    void deleteById(UUID id);
    void flush();
    Optional<OperacionPendiente> findByClaveIdempotencia(String claveIdempotencia);
    Optional<OperacionPendiente> findFirstByIdReservaAndEstadoIn(UUID idReserva, Collection<String> estados);
    List<OperacionPendiente> findTop25ByEstadoAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(String estado, OffsetDateTime fecha);
}
