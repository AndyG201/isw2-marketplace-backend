package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.marketplace.publicacion.domain.SolicitudCambioPublicacion;
import co.edu.unbosque.marketplace.publicacion.application.port.SolicitudCambioPublicacionRepository;

public interface SolicitudCambioPublicacionRepositoryJpa extends JpaRepository<SolicitudCambioPublicacion, UUID>,
        SolicitudCambioPublicacionRepository {

    Optional<SolicitudCambioPublicacion>
            findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(UUID idPublicacion);
}
