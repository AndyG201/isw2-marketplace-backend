package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;

public interface ImagenPublicacionRepository {

    <S extends ImagenPublicacion> S save(S entidad);
    <S extends ImagenPublicacion> List<S> saveAll(Iterable<S> entidades);
    <S extends ImagenPublicacion> S saveAndFlush(S entidad);
    Optional<ImagenPublicacion> findById(UUID id);
    List<ImagenPublicacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(ImagenPublicacion entidad);
    void deleteById(UUID id);
    void flush();
    void deleteAll(Iterable<? extends ImagenPublicacion> entidades);

    List<ImagenPublicacion> findByIdPublicacionOrderByOrdenAsc(UUID idPublicacion);
    List<ImagenPublicacion> findByIdPublicacionInOrderByOrdenAsc(Collection<UUID> idsPublicaciones);
    boolean existsByIdPublicacion(UUID idPublicacion);
}
