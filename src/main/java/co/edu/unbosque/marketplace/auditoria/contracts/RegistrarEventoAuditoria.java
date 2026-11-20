package co.edu.unbosque.marketplace.auditoria.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistrarEventoAuditoria(
        UUID idUsuario,
        OffsetDateTime fechaEvento,
        String accion,
        String resultado,
        String entidadAfectada,
        UUID idEntidadAfectada,
        String detalle,
        String idCorrelacion) {
}