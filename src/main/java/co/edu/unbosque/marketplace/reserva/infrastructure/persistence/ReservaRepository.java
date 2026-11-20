package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@Repository
public interface ReservaRepository
        extends JpaRepository<Reserva, UUID> {

    List<Reserva> findByIdCotizacionIn(
            Collection<UUID> idsCotizaciones
    );

    @Query(value = """
            SELECT r.*
            FROM public.reserva r
            INNER JOIN public.cotizacion c
                ON c.id_cotizacion = r.id_cotizacion
            WHERE r.estado = 'CONFIRMADA'
              AND c.fecha_salida <= :hoy
            """, nativeQuery = true)
    List<Reserva> buscarConfirmadasFinalizadas(@Param("hoy") LocalDate hoy);
}
