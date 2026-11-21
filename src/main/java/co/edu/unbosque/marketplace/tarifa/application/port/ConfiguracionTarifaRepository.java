package co.edu.unbosque.marketplace.tarifa.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.tarifa.domain.ConfiguracionTarifa;

public interface ConfiguracionTarifaRepository {

    <S extends ConfiguracionTarifa> S save(S entidad);
    <S extends ConfiguracionTarifa> List<S> saveAll(Iterable<S> entidades);
    <S extends ConfiguracionTarifa> S saveAndFlush(S entidad);
    Optional<ConfiguracionTarifa> findById(UUID id);
    List<ConfiguracionTarifa> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(ConfiguracionTarifa entidad);
    void deleteById(UUID id);
    void flush();
    Optional<ConfiguracionTarifa> findFirstByVigenteHastaIsNullAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(OffsetDateTime instante);
    List<ConfiguracionTarifa> bloquearConfiguracionesVigentes(OffsetDateTime instante);
}
