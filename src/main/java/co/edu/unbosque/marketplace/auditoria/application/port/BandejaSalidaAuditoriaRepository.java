package co.edu.unbosque.marketplace.auditoria.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

public interface BandejaSalidaAuditoriaRepository extends RepositorioBasico<BandejaSalidaAuditoria, UUID> {
    List<BandejaSalidaAuditoria> findTop100ByEstadoEnvioInAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(List<String> estados, OffsetDateTime fecha);
}
