package co.edu.unbosque.marketplace.auditoria.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FiltrosAuditoria(
        UUID idUsuario,
        OffsetDateTime desde,
        OffsetDateTime hasta,
        String accion,
        String resultado,
        String entidadAfectada) {
}