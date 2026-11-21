package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaSolicitudRepository;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaSolicitudDatos;

@Repository
public interface ReservaSolicitudRepositoryJpa extends JpaRepository<Reserva, UUID>,
        ReservaSolicitudRepository {

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
            FROM public.vista_cotizacion c
            INNER JOIN public.vista_publicacion p
                ON p.id_publicacion = c.id_publicacion
            INNER JOIN public.vista_alojamiento a
                ON a.id_alojamiento = p.id_alojamiento
            WHERE c.id_cotizacion = :idCotizacion
            """, nativeQuery = true)
    Optional<ReservaSolicitudDatos> consultarDatosCotizacion(
            @Param("idCotizacion") UUID idCotizacion);
}
