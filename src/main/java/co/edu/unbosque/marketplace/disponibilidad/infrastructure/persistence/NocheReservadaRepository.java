package co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.disponibilidad.domain.NocheReservada;

@Repository
public interface NocheReservadaRepository
        extends JpaRepository<NocheReservada, UUID> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM public.noche_reservada nr
                INNER JOIN public.reserva_noche rn
                    ON rn.id_noche = nr.id_noche
                INNER JOIN public.reserva r
                    ON r.id_reserva = rn.id_reserva
                WHERE nr.id_alojamiento = :idAlojamiento
                AND nr.fecha >= :fechaInicio
                AND nr.fecha < :fechaFin
                AND (
                    r.estado IN (
                        'PENDIENTE_APROBACION',
                        'CONFIRMADA',
                        'COMPLETADA'
                    )
                    OR (
                        r.estado = 'PENDIENTE_PAGO'
                        AND r.retencion_hasta > CURRENT_TIMESTAMP
                    )
                )
            )
            """, nativeQuery = true)
    boolean existeNocheOcupadaEnRango(
            @Param("idAlojamiento") UUID idAlojamiento,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}
