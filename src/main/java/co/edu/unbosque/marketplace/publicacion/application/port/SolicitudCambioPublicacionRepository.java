package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.publicacion.domain.SolicitudCambioPublicacion;

public interface SolicitudCambioPublicacionRepository extends RepositorioBasico<SolicitudCambioPublicacion, UUID> {
    Optional<SolicitudCambioPublicacion> findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(UUID idPublicacion);
}
