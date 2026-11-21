package co.edu.unbosque.marketplace.pago.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.pago.domain.ReembolsoPago;

public interface ReembolsoPagoRepository {

    <S extends ReembolsoPago> S save(S entidad);
    <S extends ReembolsoPago> List<S> saveAll(Iterable<S> entidades);
    <S extends ReembolsoPago> S saveAndFlush(S entidad);
    Optional<ReembolsoPago> findById(UUID id);
    List<ReembolsoPago> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(ReembolsoPago entidad);
    void deleteById(UUID id);
    void flush();
    Optional<ReembolsoPago> findByIdPago(UUID idPago);
}
