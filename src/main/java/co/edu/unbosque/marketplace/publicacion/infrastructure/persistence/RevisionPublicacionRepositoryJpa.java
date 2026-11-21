
package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.application.port.RevisionPublicacionRepository;

@Repository
public interface RevisionPublicacionRepositoryJpa extends JpaRepository<RevisionPublicacion, UUID>,
        RevisionPublicacionRepository {

    List<RevisionPublicacion> findByIdPublicacionOrderByFechaRevisionDesc(
            UUID idPublicacion
    );
}
