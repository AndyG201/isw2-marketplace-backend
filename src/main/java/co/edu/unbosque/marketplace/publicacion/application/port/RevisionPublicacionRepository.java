package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;

public interface RevisionPublicacionRepository {

    <S extends RevisionPublicacion> S save(S entidad);
    <S extends RevisionPublicacion> List<S> saveAll(Iterable<S> entidades);
    <S extends RevisionPublicacion> S saveAndFlush(S entidad);
    Optional<RevisionPublicacion> findById(UUID id);
    List<RevisionPublicacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(RevisionPublicacion entidad);
    void deleteById(UUID id);
    void flush();
    List<RevisionPublicacion> findByIdPublicacionOrderByFechaRevisionDesc(UUID idPublicacion);
}
