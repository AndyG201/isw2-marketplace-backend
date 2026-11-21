package co.edu.unbosque.marketplace.resena.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.resena.domain.Resena;

public interface ResenaModeracionRepository {

    <S extends Resena> S save(S entidad);
    <S extends Resena> List<S> saveAll(Iterable<S> entidades);
    <S extends Resena> S saveAndFlush(S entidad);
    Optional<Resena> findById(UUID id);
    List<Resena> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Resena entidad);
    void deleteById(UUID id);
    void flush();
    Optional<Resena> bloquearPorId(UUID idResena);
}
