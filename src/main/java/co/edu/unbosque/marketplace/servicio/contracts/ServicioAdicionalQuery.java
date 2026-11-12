package co.edu.unbosque.marketplace.servicio.contracts;

import java.util.List;
import java.util.UUID;

public interface ServicioAdicionalQuery {

    List<ServicioAdicionalInfo> consultarActivosPorPublicacion(
            UUID idPublicacion
    );
}
