package co.edu.unbosque.marketplace.reserva.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@Repository
public interface ReservaRetencionRepository extends JpaRepository<Reserva, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.idReserva = :id")
    Optional<Reserva> bloquearPorId(@Param("id") UUID id);

    @Query(value = """
            SELECT r.id_reserva FROM public.reserva r
            WHERE r.estado = 'PENDIENTE_APROBACION'
              AND r.retencion_hasta IS NOT NULL
              AND r.retencion_hasta <= :ahora
            """, nativeQuery = true)
    List<UUID> buscarRetencionesVencidas(@Param("ahora") OffsetDateTime ahora);
}
