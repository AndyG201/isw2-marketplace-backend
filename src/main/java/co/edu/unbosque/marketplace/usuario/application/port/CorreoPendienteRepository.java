package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.usuario.domain.CorreoPendiente;

public interface CorreoPendienteRepository {

    <S extends CorreoPendiente> S save(S entidad);
    <S extends CorreoPendiente> List<S> saveAll(Iterable<S> entidades);
    <S extends CorreoPendiente> S saveAndFlush(S entidad);
    Optional<CorreoPendiente> findById(UUID id);
    List<CorreoPendiente> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(CorreoPendiente entidad);
    void deleteById(UUID id);
    void flush();
    List<CorreoPendiente> reclamar(OffsetDateTime ahora, org.springframework.data.domain.Pageable pageable);
}
