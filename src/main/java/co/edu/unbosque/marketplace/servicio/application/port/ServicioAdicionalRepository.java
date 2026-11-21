package co.edu.unbosque.marketplace.servicio.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.servicio.domain.ServicioAdicional;

public interface ServicioAdicionalRepository {

    <S extends ServicioAdicional> S save(S entidad);
    <S extends ServicioAdicional> List<S> saveAll(Iterable<S> entidades);
    <S extends ServicioAdicional> S saveAndFlush(S entidad);
    Optional<ServicioAdicional> findById(UUID id);
    List<ServicioAdicional> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(ServicioAdicional entidad);
    void deleteById(UUID id);
    void flush();
    List<ServicioAdicional> findByIdPublicacionAndActivoTrueOrderByNombreAsc(UUID idPublicacion);
}
