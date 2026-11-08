
package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

@Repository
public interface BandejaSalidaAuditoriaRepository
        extends JpaRepository<BandejaSalidaAuditoria, UUID> {

}
