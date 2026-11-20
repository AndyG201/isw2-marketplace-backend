
package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

@Repository
public interface BandejaSalidaAuditoriaRepository
        extends JpaRepository<BandejaSalidaAuditoria, UUID> {

    List<BandejaSalidaAuditoria> findTop100ByEstadoEnvioInAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(
            List<String> estados, OffsetDateTime fecha);
}
