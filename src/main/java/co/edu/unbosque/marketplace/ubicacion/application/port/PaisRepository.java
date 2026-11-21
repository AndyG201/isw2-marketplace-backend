package co.edu.unbosque.marketplace.ubicacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.ubicacion.domain.Pais;

public interface PaisRepository {

    <S extends Pais> S save(S entidad);
    <S extends Pais> List<S> saveAll(Iterable<S> entidades);
    <S extends Pais> S saveAndFlush(S entidad);
    Optional<Pais> findById(UUID id);
    List<Pais> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Pais entidad);
    void deleteById(UUID id);
    void flush();
    List<Pais> findAllByOrderByNombreAsc();
    Optional<Pais> findByCodigoIso2IgnoreCase(String codigoIso2);
}
