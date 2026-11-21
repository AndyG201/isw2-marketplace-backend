package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.marketplace.publicacion.domain.SolicitudCambioPublicacion;

public interface SolicitudCambioPublicacionRepository
        extends JpaRepository<SolicitudCambioPublicacion, UUID> {

    Optional<SolicitudCambioPublicacion>
            findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(UUID idPublicacion);
}
