package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;

public interface ImagenPublicacionRepository extends RepositorioBasico<ImagenPublicacion, UUID> {
    void deleteAll(Iterable<? extends ImagenPublicacion> entidades);

    List<ImagenPublicacion> findByIdPublicacionOrderByOrdenAsc(UUID idPublicacion);
    List<ImagenPublicacion> findByIdPublicacionInOrderByOrdenAsc(Collection<UUID> idsPublicaciones);
    boolean existsByIdPublicacion(UUID idPublicacion);
}
