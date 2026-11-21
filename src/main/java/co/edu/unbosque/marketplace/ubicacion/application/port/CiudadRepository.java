package co.edu.unbosque.marketplace.ubicacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.ubicacion.domain.Ciudad;

public interface CiudadRepository {

    <S extends Ciudad> S save(S entidad);
    <S extends Ciudad> List<S> saveAll(Iterable<S> entidades);
    <S extends Ciudad> S saveAndFlush(S entidad);
    Optional<Ciudad> findById(UUID id);
    List<Ciudad> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Ciudad entidad);
    void deleteById(UUID id);
    void flush();
    List<Ciudad> findAllByPaisIdPaisOrderByNombreAsc(UUID idPais);
    boolean existsByIdCiudad(UUID idCiudad);
    boolean existsByPaisIdPais(UUID idPais);
}
