package co.edu.unbosque.marketplace.resena.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ResenaPublicacionItemResponse(
        UUID idResena,
        int calificacion,
        String comentario,
        OffsetDateTime fechaPublicacion) {
}
