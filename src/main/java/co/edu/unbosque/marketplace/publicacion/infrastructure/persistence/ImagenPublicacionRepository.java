
package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;

@Repository
public interface ImagenPublicacionRepository
        extends JpaRepository<ImagenPublicacion, UUID> {

    List<ImagenPublicacion> findByIdPublicacionOrderByOrdenAsc(
            UUID idPublicacion
    );

    List<ImagenPublicacion> findByIdPublicacionInOrderByOrdenAsc(
            Collection<UUID> idsPublicaciones
    );

    boolean existsByIdPublicacion(UUID idPublicacion);
}
