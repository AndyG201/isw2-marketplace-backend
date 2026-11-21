package co.edu.unbosque.marketplace.notificacion.application.port;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CancelacionPendienteNotificacion(
        UUID idCancelacion,
        UUID idReserva,
        UUID idUsuario,
        OffsetDateTime fechaCancelacion,
        String motivo) {
}
