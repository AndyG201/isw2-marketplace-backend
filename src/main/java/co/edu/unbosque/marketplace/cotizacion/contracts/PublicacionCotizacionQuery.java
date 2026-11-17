package co.edu.unbosque.marketplace.cotizacion.contracts;

import java.util.Optional;
import java.util.UUID;

public interface PublicacionCotizacionQuery {

    Optional<PublicacionCotizacionInfo> consultarPublicacionActiva(
            UUID idPublicacion
    );
}