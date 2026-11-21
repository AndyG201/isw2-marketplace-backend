package co.edu.unbosque.marketplace.cotizacion.application.port;

import java.util.List;
import java.util.Optional;

import co.edu.unbosque.marketplace.cotizacion.domain.CotizacionAdicional;
import co.edu.unbosque.marketplace.cotizacion.domain.CotizacionAdicionalId;

public interface CotizacionAdicionalRepository {

    <S extends CotizacionAdicional> S save(S entidad);
    <S extends CotizacionAdicional> List<S> saveAll(Iterable<S> entidades);
    <S extends CotizacionAdicional> S saveAndFlush(S entidad);
    Optional<CotizacionAdicional> findById(CotizacionAdicionalId id);
    List<CotizacionAdicional> findAll();
    boolean existsById(CotizacionAdicionalId id);
    long count();
    void delete(CotizacionAdicional entidad);
    void deleteById(CotizacionAdicionalId id);
    void flush();
}
