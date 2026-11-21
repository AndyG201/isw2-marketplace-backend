package co.edu.unbosque.marketplace.cancelacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;

public interface PoliticaCancelacionRepository {

    <S extends PoliticaCancelacion> S save(S entidad);
    <S extends PoliticaCancelacion> List<S> saveAll(Iterable<S> entidades);
    <S extends PoliticaCancelacion> S saveAndFlush(S entidad);
    Optional<PoliticaCancelacion> findById(UUID id);
    List<PoliticaCancelacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(PoliticaCancelacion entidad);
    void deleteById(UUID id);
    void flush();
    Optional<PoliticaCancelacion> findByIdPoliticaAndActivaTrue(UUID idPolitica);
    List<PoliticaCancelacion> findByActivaTrueOrderByNombreAsc();
}
