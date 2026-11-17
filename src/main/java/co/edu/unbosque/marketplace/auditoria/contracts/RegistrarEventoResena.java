package co.edu.unbosque.marketplace.auditoria.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistrarEventoResena(
        UUID idUsuario,
        UUID idResena,
        OffsetDateTime fechaEvento,
        String accion,
        String resultado,
        String detalle) {
}
