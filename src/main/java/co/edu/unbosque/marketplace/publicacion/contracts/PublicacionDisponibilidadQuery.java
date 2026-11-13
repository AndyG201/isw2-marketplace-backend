package co.edu.unbosque.marketplace.publicacion.contracts;

import java.util.UUID;

public interface PublicacionDisponibilidadQuery {

    boolean existePublicacionActivaPorAlojamiento(
            UUID idAlojamiento
    );
}
