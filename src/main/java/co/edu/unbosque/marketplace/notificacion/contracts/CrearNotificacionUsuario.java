package co.edu.unbosque.marketplace.notificacion.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CrearNotificacionUsuario(
        UUID idUsuario,
        String tipo,
        String mensaje,
        OffsetDateTime fechaCreacion) {
}