
package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@Repository
public interface PublicacionRepository
        extends JpaRepository<Publicacion, UUID> {

    Optional<Publicacion> findByIdAlojamiento(UUID idAlojamiento);
}
