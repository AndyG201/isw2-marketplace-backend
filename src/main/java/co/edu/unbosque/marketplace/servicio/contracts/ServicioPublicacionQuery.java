
package co.edu.unbosque.marketplace.servicio.contracts;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ServicioPublicacionQuery {

    List<ServicioInfo> consultarPorIds(
            Collection<UUID> ids
    );
}
