package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;

public interface RevisionPublicacionRepository extends RepositorioBasico<RevisionPublicacion, UUID> {
    List<RevisionPublicacion> findByIdPublicacionOrderByFechaRevisionDesc(UUID idPublicacion);
}
