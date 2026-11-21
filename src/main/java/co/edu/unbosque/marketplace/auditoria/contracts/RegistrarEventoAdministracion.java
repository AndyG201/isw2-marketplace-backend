package co.edu.unbosque.marketplace.auditoria.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistrarEventoAdministracion(
        UUID idUsuario,
        UUID idEntidadAfectada,
        String entidadAfectada,
        OffsetDateTime fechaEvento,
        String accion,
        String resultado,
        String detalle,
        String idCorrelacion) {
}