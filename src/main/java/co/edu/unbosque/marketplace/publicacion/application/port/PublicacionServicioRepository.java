package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicioId;

public interface PublicacionServicioRepository {

    <S extends PublicacionServicio> S save(S entidad);
    <S extends PublicacionServicio> List<S> saveAll(Iterable<S> entidades);
    <S extends PublicacionServicio> S saveAndFlush(S entidad);
    Optional<PublicacionServicio> findById(PublicacionServicioId id);
    List<PublicacionServicio> findAll();
    boolean existsById(PublicacionServicioId id);
    long count();
    void delete(PublicacionServicio entidad);
    void deleteById(PublicacionServicioId id);
    void flush();
    void deleteAll(Iterable<? extends PublicacionServicio> entidades);

    List<PublicacionServicio> findByIdPublicacion(UUID idPublicacion);
    boolean existsByIdPublicacionAndIdServicio(UUID idPublicacion, UUID idServicio);
}
