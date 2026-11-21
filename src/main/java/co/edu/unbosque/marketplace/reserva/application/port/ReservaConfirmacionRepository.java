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
public interface ReservaConfirmacionRepository extends JpaRepository<Reserva, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.idReserva = :id")
    Optional<Reserva> bloquearPorId(@Param("id") UUID id);

    @Query(value = """
            SELECT a.id_alojamiento AS "idAlojamiento",
                   c.fecha_entrada AS "fechaEntrada",
                   c.fecha_salida AS "fechaSalida",
                   ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
                    + c.tarifa_limpieza
                    + ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
                       * ct.porcentaje_servicio / 100)
                    - ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
                       * ct.porcentaje_descuento / 100)
                    + COALESCE((SELECT SUM(ca.cantidad * ca.precio_aplicado)
                                FROM public.cotizacion_adicional ca
                                WHERE ca.id_cotizacion = c.id_cotizacion), 0)) AS "montoTotal"
            FROM public.cotizacion c
            JOIN public.publicacion p ON p.id_publicacion = c.id_publicacion
            JOIN public.alojamiento a ON a.id_alojamiento = p.id_alojamiento
            JOIN public.configuracion_tarifa ct ON ct.id_configuracion = c.id_configuracion
            WHERE c.id_cotizacion = :idCotizacion
            """, nativeQuery = true)
    Optional<ReservaConfirmacionDatos> consultarDatos(@Param("idCotizacion") UUID idCotizacion);

    @Query(value = """
            SELECT r.id_reserva FROM public.reserva r
            WHERE r.estado = 'PENDIENTE_PAGO'
              AND r.retencion_hasta IS NOT NULL
              AND r.retencion_hasta <= :ahora
            """, nativeQuery = true)
    List<UUID> buscarRetencionesVencidas(@Param("ahora") OffsetDateTime ahora);
}
