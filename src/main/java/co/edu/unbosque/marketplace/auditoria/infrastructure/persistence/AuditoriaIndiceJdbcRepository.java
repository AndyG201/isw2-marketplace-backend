
package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.storage.ResultadoAlmacenamientoAuditoria;

/**
 * Registra el índice de los eventos archivados en Backblaze.
 *
 * PostgreSQL conserva los metadatos para permitir búsquedas
 * sin tener que recorrer todos los objetos del bucket.
 */
@Repository
public class AuditoriaIndiceJdbcRepository {

    private static final String INSERTAR = """
            INSERT INTO public.evento_auditoria (
                id_evento,
                id_usuario,
                fecha_evento,
                accion,
                resultado,
                entidad_afectada,
                id_entidad_afectada,
                id_correlacion,
                clave_objeto,
                sha256,
                modo_retencion,
                retencion_hasta,
                version_objeto
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id_evento) DO NOTHING
            """;

    private final JdbcTemplate jdbc;

    public AuditoriaIndiceJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(
            BandejaSalidaAuditoria evento,
            ResultadoAlmacenamientoAuditoria almacenamiento) {

        if (evento == null || almacenamiento == null) {
            throw new IllegalArgumentException(
                    "El evento y su resultado de almacenamiento son obligatorios.");
        }

        if (!"COMPLIANCE".equals(almacenamiento.modoRetencion())
                || almacenamiento.retencionHasta() == null) {

            throw new IllegalStateException(
                    "El evento no tiene una retención COMPLIANCE confirmada.");
        }

        int insertados = jdbc.update(
                INSERTAR,
                evento.getIdEvento(),
                evento.getIdUsuario(),
                evento.getFechaEvento(),
                evento.getAccion(),
                evento.getResultado(),
                evento.getEntidadAfectada(),
                evento.getIdEntidadAfectada(),
                evento.getIdCorrelacion(),
                almacenamiento.claveObjeto(),
                almacenamiento.sha256(),
                almacenamiento.modoRetencion(),
                almacenamiento.retencionHasta(),
                almacenamiento.versionId()
        );

        // Si es un reintento, se comprueba que el índice existente
        // corresponda exactamente al mismo objeto.
        if (insertados == 0) {

            Boolean coincide = jdbc.queryForObject(
                    """
                    SELECT EXISTS (
                        SELECT 1
                        FROM public.evento_auditoria
                        WHERE id_evento = ?
                          AND clave_objeto = ?
                          AND sha256 = ?
                          AND version_objeto IS NOT DISTINCT FROM ?
                    )
                    """,
                    Boolean.class,
                    evento.getIdEvento(),
                    almacenamiento.claveObjeto(),
                    almacenamiento.sha256(),
                    almacenamiento.versionId()
            );

            if (!Boolean.TRUE.equals(coincide)) {
                throw new IllegalStateException(
                        "Existe un índice de auditoría con información diferente.");
            }
        }
    }
}
