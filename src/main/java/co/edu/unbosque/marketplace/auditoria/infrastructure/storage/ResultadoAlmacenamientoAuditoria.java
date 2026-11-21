
package co.edu.unbosque.marketplace.auditoria.infrastructure.storage;

import java.time.OffsetDateTime;

/**
 * Resultado de almacenar un evento en Backblaze B2.
 */
public record ResultadoAlmacenamientoAuditoria(
        String claveObjeto,
        String sha256,
        String modoRetencion,
        OffsetDateTime retencionHasta,
        String versionId) {
}
