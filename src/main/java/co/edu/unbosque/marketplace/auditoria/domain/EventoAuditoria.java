package co.edu.unbosque.marketplace.auditoria.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventoAuditoria(
        UUID idEvento,
        UUID idUsuario,
        OffsetDateTime fechaEvento,
        String accion,
        String resultado,
        String entidadAfectada,
        UUID idEntidadAfectada,
        String detalleJson,
        String idCorrelacion) {
}