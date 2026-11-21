package co.edu.unbosque.marketplace.alojamiento.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;

public interface AlojamientoRepository {

    <S extends Alojamiento> S save(S entidad);
    <S extends Alojamiento> List<S> saveAll(Iterable<S> entidades);
    <S extends Alojamiento> S saveAndFlush(S entidad);
    Optional<Alojamiento> findById(UUID id);
    List<Alojamiento> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Alojamiento entidad);
    void deleteById(UUID id);
    void flush();
    List<Alojamiento> findAllById(Iterable<UUID> ids);

    List<Alojamiento> findByIdAnfitrion(UUID idAnfitrion);
}
