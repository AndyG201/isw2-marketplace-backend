package co.edu.unbosque.marketplace.auditoria.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventoAuditoriaResponse(
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