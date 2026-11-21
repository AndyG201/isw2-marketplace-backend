package co.edu.unbosque.marketplace.auditoria.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

public interface BandejaSalidaAuditoriaRepository {

    <S extends BandejaSalidaAuditoria> S save(S entidad);
    <S extends BandejaSalidaAuditoria> List<S> saveAll(Iterable<S> entidades);
    <S extends BandejaSalidaAuditoria> S saveAndFlush(S entidad);
    Optional<BandejaSalidaAuditoria> findById(UUID id);
    List<BandejaSalidaAuditoria> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(BandejaSalidaAuditoria entidad);
    void deleteById(UUID id);
    void flush();
    List<BandejaSalidaAuditoria> findTop100ByEstadoEnvioInAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(List<String> estados, OffsetDateTime fecha);
}
