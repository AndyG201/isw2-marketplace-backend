package co.edu.unbosque.marketplace.cotizacion.contracts;

import java.util.List;
import java.util.UUID;

public interface CotizacionReservaQuery {

    List<CotizacionReservaInfo> consultarPorPublicacion(
            UUID idPublicacion
    );
}
