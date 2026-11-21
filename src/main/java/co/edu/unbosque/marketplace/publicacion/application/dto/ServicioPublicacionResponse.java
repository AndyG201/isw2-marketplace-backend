
package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.util.UUID;

public record ServicioPublicacionResponse(
        UUID idServicio,
        String nombre,
        String descripcion
) {
}
