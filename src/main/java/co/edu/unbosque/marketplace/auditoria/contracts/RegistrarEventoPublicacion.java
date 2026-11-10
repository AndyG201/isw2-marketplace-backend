
package co.edu.unbosque.marketplace.auditoria.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistrarEventoPublicacion(
        UUID idUsuario,
        UUID idPublicacion,
        OffsetDateTime fechaEvento,
        String accion,
        String resultado,
        String detalle
) {
}
