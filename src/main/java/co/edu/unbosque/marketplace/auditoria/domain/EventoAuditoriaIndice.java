
package co.edu.unbosque.marketplace.auditoria.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa el índice de un evento almacenado en Backblaze B2.
 * PostgreSQL conserva los metadatos necesarios para buscarlo.
 */
public record EventoAuditoriaIndice(
        UUID idEvento,
        UUID idUsuario,
        OffsetDateTime fechaEvento,
        String accion,
        String resultado,
        String entidadAfectada,
        UUID idEntidadAfectada,
        String idCorrelacion,
        String claveObjeto,
        String sha256,
        String versionObjeto) {
}
