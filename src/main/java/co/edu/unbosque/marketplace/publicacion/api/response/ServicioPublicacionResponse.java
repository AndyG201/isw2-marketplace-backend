
package co.edu.unbosque.marketplace.publicacion.api.response;

import java.util.UUID;

public record ServicioPublicacionResponse(
        UUID idServicio,
        String nombre,
        String descripcion
) {
}
