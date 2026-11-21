package co.edu.unbosque.marketplace.cancelacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.cancelacion.domain.Cancelacion;

public interface CancelacionRepository {

    <S extends Cancelacion> S save(S entidad);
    <S extends Cancelacion> List<S> saveAll(Iterable<S> entidades);
    <S extends Cancelacion> S saveAndFlush(S entidad);
    Optional<Cancelacion> findById(UUID id);
    List<Cancelacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Cancelacion entidad);
    void deleteById(UUID id);
    void flush();
    Optional<Cancelacion> findByIdReserva(UUID idReserva);
}
