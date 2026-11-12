package co.edu.unbosque.marketplace.servicio.contracts;

import java.util.List;
import java.util.UUID;

public interface ServicioAdicionalCommand {

    List<ServicioAdicionalInfo> reemplazarActivosPorPublicacion(
            UUID idPublicacion,
            List<ServicioAdicionalData> servicios
    );
}
