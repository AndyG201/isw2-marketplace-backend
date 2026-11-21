package co.edu.unbosque.marketplace.tarifa.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.tarifa.domain.ConfiguracionTarifa;

public interface ConfiguracionTarifaRepository extends RepositorioBasico<ConfiguracionTarifa, UUID> {
    Optional<ConfiguracionTarifa> findFirstByVigenteHastaIsNullAndVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(OffsetDateTime instante);
    List<ConfiguracionTarifa> bloquearConfiguracionesVigentes(OffsetDateTime instante);
}
