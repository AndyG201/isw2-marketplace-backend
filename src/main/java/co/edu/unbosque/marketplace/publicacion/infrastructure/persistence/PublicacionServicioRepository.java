package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicioId;

@Repository
public interface PublicacionServicioRepository
        extends JpaRepository<PublicacionServicio, PublicacionServicioId> {

    List<PublicacionServicio> findByIdPublicacion(UUID idPublicacion);

    boolean existsByIdPublicacionAndIdServicio(
            UUID idPublicacion,
            UUID idServicio
    );
}
