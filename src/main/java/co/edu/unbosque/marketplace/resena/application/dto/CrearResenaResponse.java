package co.edu.unbosque.marketplace.resena.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CrearResenaResponse(
        UUID idResena,
        UUID idReserva,
        UUID idHuesped,
        UUID idAlojamiento,
        UUID idPublicacion,
        int calificacion,
        String comentario,
        boolean visible,
        OffsetDateTime fechaPublicacion,
        String mensaje) {
}
