
package co.edu.unbosque.marketplace.servicio.contracts;

import java.util.UUID;

public record ServicioInfo(
        UUID idServicio,
        String nombre,
        String descripcion
) {
}
