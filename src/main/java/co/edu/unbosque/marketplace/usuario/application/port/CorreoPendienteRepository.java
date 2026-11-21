package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import co.edu.unbosque.marketplace.usuario.domain.CorreoPendiente;

public interface CorreoPendienteRepository extends JpaRepository<CorreoPendiente, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select c from CorreoPendiente c
        where ((c.estado in ('PENDIENTE','ERROR') and c.proximaEjecucion <= :ahora)
            or (c.estado = 'EN_PROCESO' and c.leaseHasta <= :ahora))
        order by c.proximaEjecucion asc
        """)
    List<CorreoPendiente> reclamar(@Param("ahora") OffsetDateTime ahora,
                                   org.springframework.data.domain.Pageable pageable);
}
