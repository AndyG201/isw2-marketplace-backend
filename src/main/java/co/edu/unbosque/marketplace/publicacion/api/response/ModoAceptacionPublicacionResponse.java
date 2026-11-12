package co.edu.unbosque.marketplace.publicacion.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ModoAceptacionPublicacionResponse(
        UUID idPublicacion,
        String modoAceptacion,
        OffsetDateTime fechaActualizacion
) {
}
