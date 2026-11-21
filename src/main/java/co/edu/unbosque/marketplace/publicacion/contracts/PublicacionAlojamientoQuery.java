package co.edu.unbosque.marketplace.publicacion.contracts;

import java.util.Optional;
import java.util.UUID;

public interface PublicacionAlojamientoQuery {

    Optional<UUID> consultarIdAlojamiento(UUID idPublicacion);
}
