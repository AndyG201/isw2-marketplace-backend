package co.edu.unbosque.marketplace.tarifa.contracts;

import java.util.Optional;

public interface ConfiguracionTarifaQuery {

    Optional<ConfiguracionTarifaInfo> consultarConfiguracionVigente();
}