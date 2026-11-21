package co.edu.unbosque.marketplace.cotizacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;

public interface CotizacionRepository {

    <S extends Cotizacion> S save(S entidad);
    <S extends Cotizacion> List<S> saveAll(Iterable<S> entidades);
    <S extends Cotizacion> S saveAndFlush(S entidad);
    Optional<Cotizacion> findById(UUID id);
    List<Cotizacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Cotizacion entidad);
    void deleteById(UUID id);
    void flush();
    List<Cotizacion> findByIdPublicacion(UUID idPublicacion);
}
