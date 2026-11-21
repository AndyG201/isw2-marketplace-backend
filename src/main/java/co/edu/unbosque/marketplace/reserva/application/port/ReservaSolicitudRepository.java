package co.edu.unbosque.marketplace.reserva.application.port;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@Repository
public interface ReservaSolicitudRepository
        extends JpaRepository<Reserva, UUID> {

    Optional<Reserva> findByClaveIdempotencia(String claveIdempotencia);

    Optional<Reserva> findByIdCotizacion(UUID idCotizacion);

    @Query(value = """
            SELECT
                c.id_cotizacion AS "idCotizacion",
                c.id_publicacion AS "idPublicacion",
                p.id_alojamiento AS "idAlojamiento",
                p.id_politica AS "idPolitica",
                a.id_anfitrion AS "idAnfitrion",
                p.modo_aceptacion AS "modoAceptacion",
                p.estado AS "estadoPublicacion",
                a.capacidad AS "capacidad",
                c.fecha_entrada AS "fechaEntrada",
                c.fecha_salida AS "fechaSalida",
                c.cantidad_huespedes AS "cantidadHuespedes"
            FROM public.cotizacion c
            INNER JOIN public.publicacion p
                ON p.id_publicacion = c.id_publicacion
            INNER JOIN public.alojamiento a
                ON a.id_alojamiento = p.id_alojamiento
            WHERE c.id_cotizacion = :idCotizacion
            """, nativeQuery = true)
    Optional<ReservaSolicitudDatos> consultarDatosCotizacion(
            @Param("idCotizacion") UUID idCotizacion);

    @Query(value = """
            SELECT NOT EXISTS (
                SELECT 1
                FROM generate_series(
                    CAST(:fechaEntrada AS timestamp),
                    CAST(:fechaSalida AS timestamp) - INTERVAL '1 day',
                    INTERVAL '1 day'
                ) AS fechas(noche)
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM public.periodo_disponibilidad pd
                    WHERE pd.id_alojamiento = :idAlojamiento
                      AND pd.habilitado = true
                      AND pd.fecha_inicio <= CAST(fechas.noche AS date)
                      AND pd.fecha_fin > CAST(fechas.noche AS date)
                )
                OR EXISTS (
                    SELECT 1
                    FROM public.noche_reservada nr
                    INNER JOIN public.reserva_noche rn
                        ON rn.id_noche = nr.id_noche
                    INNER JOIN public.reserva r
                        ON r.id_reserva = rn.id_reserva
                    WHERE nr.id_alojamiento = :idAlojamiento
                      AND nr.fecha = CAST(fechas.noche AS date)
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
            )
            """, nativeQuery = true)
    boolean rangoDisponible(
            @Param("idAlojamiento") UUID idAlojamiento,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("fechaSalida") LocalDate fechaSalida);
}