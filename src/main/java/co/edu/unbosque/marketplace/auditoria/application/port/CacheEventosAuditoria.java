package co.edu.unbosque.marketplace.auditoria.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;

/**
 * Caché de eventos de auditoría ya recuperados del almacén inmutable. Un evento archivado nunca cambia, así que una vez
 * leído y verificado se puede reutilizar sin volver a descargarlo. La clave incluye el SHA-256 registrado en el índice:
 * si el índice apuntara a otra versión del archivo, la entrada anterior simplemente no se encuentra.
 *
 * <p>La caché es solo una aceleración: si falla o no está disponible, el sistema sigue funcionando leyendo del almacén.
 */
public interface CacheEventosAuditoria {

    /** Devuelve el evento guardado para ese id y huella, o vacío si no está (o la caché no responde). */
    Optional<EventoAuditoria> leer(UUID idEvento, String sha256);

    /** Guarda un evento ya verificado contra su huella. Nunca lanza excepciones por fallas de la caché. */
    void guardar(String sha256, EventoAuditoria evento);
}
