
package co.edu.unbosque.marketplace.alojamiento.contracts;

import java.util.UUID;

public interface AlojamientoPublicacionCommand {

    UUID crearAlojamiento(
            CrearAlojamientoParaPublicacion solicitud
    );

    void actualizarAlojamiento(
            UUID idAlojamiento,
            ActualizarAlojamientoParaPublicacion solicitud
    );
}
