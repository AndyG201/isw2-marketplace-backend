package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicioId;

public interface PublicacionServicioRepository extends RepositorioBasico<PublicacionServicio, PublicacionServicioId> {
    void deleteAll(Iterable<? extends PublicacionServicio> entidades);

    List<PublicacionServicio> findByIdPublicacion(UUID idPublicacion);
    boolean existsByIdPublicacionAndIdServicio(UUID idPublicacion, UUID idServicio);
}
