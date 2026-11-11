package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@Repository
public interface PublicacionRepository
        extends JpaRepository<Publicacion, UUID> {

    Optional<Publicacion> findByIdAlojamiento(UUID idAlojamiento);

    List<Publicacion> findByIdAlojamientoInOrderByFechaCreacionDesc(
            Collection<UUID> idsAlojamiento
    );

    Optional<Publicacion> findByIdPublicacionAndIdAlojamientoIn(
            UUID idPublicacion,
            Collection<UUID> idsAlojamiento
    );

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            UPDATE Publicacion p
            SET p.estado = 'PAUSADA',
                p.fechaActualizacion = :fechaActualizacion
            WHERE p.estado = 'ACTIVA'
            AND p.idAlojamiento IN :idsAlojamientos
            """)
    int pausarPublicacionesActivasPorAlojamientos(
            @Param("idsAlojamientos") Collection<UUID> idsAlojamientos,
            @Param("fechaActualizacion") OffsetDateTime fechaActualizacion);
    List<Publicacion> findByEstadoOrderByFechaCreacionDesc(
            String estado
    );
}
