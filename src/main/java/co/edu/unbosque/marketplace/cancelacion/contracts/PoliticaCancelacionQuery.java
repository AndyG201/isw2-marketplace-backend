package co.edu.unbosque.marketplace.cancelacion.contracts;

import java.util.Optional;
import java.util.UUID;

public interface PoliticaCancelacionQuery {

    boolean existePoliticaActiva(UUID idPolitica);

    Optional<PoliticaCancelacionInfo> consultarPoliticaSeleccionable(
            UUID idPolitica
    );

    Optional<PoliticaCancelacionInfo> consultarPorId(
            UUID idPolitica
    );
}
