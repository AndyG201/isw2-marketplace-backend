
package co.edu.unbosque.marketplace.alojamiento.contracts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlojamientoPublicacionQuery {

    Optional<UUID> consultarIdAnfitrion(UUID idAlojamiento);

    Optional<AlojamientoPublicacionInfo> consultarDatosParaPublicacion(
            UUID idAlojamiento
    );

    List<AlojamientoPublicacionInfo> consultarPorAnfitrion(
            UUID idAnfitrion
    );
}
