package co.edu.unbosque.marketplace.tarifa.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import co.edu.unbosque.marketplace.tarifa.domain.ConfiguracionTarifa;

@Repository
public interface ConfiguracionTarifaRepository
        extends JpaRepository<ConfiguracionTarifa, UUID> {

    Optional<ConfiguracionTarifa>
    findFirstByVigenteHastaIsNullAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(
            OffsetDateTime instante);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM ConfiguracionTarifa c
            WHERE c.vigenteHasta IS NULL
              AND c.vigenteDesde <= :instante
            ORDER BY c.vigenteDesde DESC
            """)
    List<ConfiguracionTarifa> bloquearConfiguracionesVigentes(
            @Param("instante") OffsetDateTime instante);
}