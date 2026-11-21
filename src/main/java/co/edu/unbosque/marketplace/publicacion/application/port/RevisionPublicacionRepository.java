
package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;

@Repository
public interface RevisionPublicacionRepository
        extends JpaRepository<RevisionPublicacion, UUID> {

    List<RevisionPublicacion> findByIdPublicacionOrderByFechaRevisionDesc(
            UUID idPublicacion
    );
}
