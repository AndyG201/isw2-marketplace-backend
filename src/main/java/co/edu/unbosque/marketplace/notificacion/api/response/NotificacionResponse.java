
package co.edu.unbosque.marketplace.notificacion.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificacionResponse(
        UUID idNotificacion,
        UUID idUsuario,
        UUID idReserva,
        UUID idPublicacion,
        String tipo,
        String mensaje,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaLectura
) {
}
