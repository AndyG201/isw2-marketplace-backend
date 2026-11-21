package co.edu.unbosque.marketplace.servicio.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import co.edu.unbosque.marketplace.servicio.domain.Servicio;

public interface ServicioRepository {

    <S extends Servicio> S save(S entidad);
    <S extends Servicio> List<S> saveAll(Iterable<S> entidades);
    <S extends Servicio> S saveAndFlush(S entidad);
    Optional<Servicio> findById(UUID id);
    List<Servicio> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Servicio entidad);
    void deleteById(UUID id);
    void flush();
    List<Servicio> findAllById(Iterable<UUID> ids);
    List<Servicio> findAll(Sort sort);
}
