package co.edu.unbosque.marketplace.pago.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.pago.domain.Pago;

public interface PagoRepository {

    <S extends Pago> S save(S entidad);
    <S extends Pago> List<S> saveAll(Iterable<S> entidades);
    <S extends Pago> S saveAndFlush(S entidad);
    Optional<Pago> findById(UUID id);
    List<Pago> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Pago entidad);
    void deleteById(UUID id);
    void flush();
    Optional<Pago> findByClaveIdempotencia(String claveIdempotencia);
    Optional<Pago> findFirstByIdReservaOrderByFechaCreacionDesc(UUID idReserva);
}
