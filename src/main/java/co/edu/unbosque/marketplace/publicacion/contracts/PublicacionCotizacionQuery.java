package co.edu.unbosque.marketplace.publicacion.contracts;

import java.util.Optional;
import java.util.UUID;

public interface PublicacionCotizacionQuery {

    Optional<PublicacionCotizacionInfo> consultarPublicacionActiva(
            UUID idPublicacion
    );
}