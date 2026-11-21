package co.edu.unbosque.marketplace.publicacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.publicacion.domain.SolicitudCambioPublicacion;

public interface SolicitudCambioPublicacionRepository {

    <S extends SolicitudCambioPublicacion> S save(S entidad);
    <S extends SolicitudCambioPublicacion> List<S> saveAll(Iterable<S> entidades);
    <S extends SolicitudCambioPublicacion> S saveAndFlush(S entidad);
    Optional<SolicitudCambioPublicacion> findById(UUID id);
    List<SolicitudCambioPublicacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(SolicitudCambioPublicacion entidad);
    void deleteById(UUID id);
    void flush();
    Optional<SolicitudCambioPublicacion> findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(UUID idPublicacion);
}
