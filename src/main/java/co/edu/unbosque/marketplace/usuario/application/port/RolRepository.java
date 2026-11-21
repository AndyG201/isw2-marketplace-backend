package co.edu.unbosque.marketplace.usuario.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.usuario.domain.Rol;

public interface RolRepository {

    <S extends Rol> S save(S entidad);
    <S extends Rol> List<S> saveAll(Iterable<S> entidades);
    <S extends Rol> S saveAndFlush(S entidad);
    Optional<Rol> findById(UUID id);
    List<Rol> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Rol entidad);
    void deleteById(UUID id);
    void flush();
    Optional<Rol> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
