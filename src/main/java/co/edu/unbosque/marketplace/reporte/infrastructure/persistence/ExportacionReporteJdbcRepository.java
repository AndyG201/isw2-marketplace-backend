package co.edu.unbosque.marketplace.reporte.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ExportacionReporteJdbcRepository {

    private final JdbcTemplate jdbc;

    public ExportacionReporteJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void crear(UUID id, UUID administrador, String formato, OffsetDateTime fecha) {
        jdbc.update("""
                INSERT INTO exportacion_reporte
                    (id_exportacion, id_administrador, tipo_reporte, formato,
                     estado, fecha_solicitud)
                VALUES (?, ?, 'DASHBOARD_GESTION', ?, 'SOLICITADO', ?)
                """, id, administrador, formato, fecha);
    }

    public Optional<ExportacionReporteInfo> buscar(UUID id, UUID administrador) {
        return jdbc.query("""
                SELECT id_exportacion, id_administrador, formato, estado,
                       fecha_solicitud, fecha_corte_datos,
                       fecha_generacion, ruta_archivo
                FROM exportacion_reporte
                WHERE id_exportacion = ? AND id_administrador = ?
                """,
                rs -> rs.next()
                        ? Optional.of(new ExportacionReporteInfo(
                                rs.getObject("id_exportacion", UUID.class),
                                rs.getObject("id_administrador", UUID.class),
                                rs.getString("formato"),
                                rs.getString("estado"),
                                rs.getObject("fecha_solicitud", OffsetDateTime.class),
                                rs.getObject("fecha_corte_datos", OffsetDateTime.class),
                                rs.getObject("fecha_generacion", OffsetDateTime.class),
                                rs.getString("ruta_archivo")))
                        : Optional.empty(),
                id, administrador);
    }

    public Optional<UUID> buscarAdministrador(UUID id) {
        return jdbc.query("""
                SELECT id_administrador
                FROM exportacion_reporte
                WHERE id_exportacion = ?
                """,
                rs -> rs.next()
                        ? Optional.of(rs.getObject(1, UUID.class))
                        : Optional.empty(),
                id);
    }

    public boolean iniciar(UUID id) {
        return jdbc.update("""
                UPDATE exportacion_reporte
                SET estado = 'EN_PROCESO', fecha_generacion = CURRENT_TIMESTAMP
                WHERE id_exportacion = ? AND estado = 'SOLICITADO'
                """, id) == 1;
    }

    public void completar(
            UUID id,
            OffsetDateTime corte,
            OffsetDateTime generacion,
            String ruta) {
        jdbc.update("""
                UPDATE exportacion_reporte
                SET estado = 'COMPLETADO',
                    fecha_corte_datos = ?,
                    fecha_generacion = ?,
                    ruta_archivo = ?
                WHERE id_exportacion = ?
                """, corte, generacion, ruta, id);
    }

    public void fallar(UUID id) {
        jdbc.update("""
                UPDATE exportacion_reporte
                SET estado = 'FALLIDO',
                    fecha_generacion = ?
                WHERE id_exportacion = ?
                """, OffsetDateTime.now(), id);
    }

    public int recuperarProcesosAbandonados(OffsetDateTime anterioresA) {
        return jdbc.update("""
                UPDATE exportacion_reporte
                SET estado = 'SOLICITADO', fecha_generacion = NULL,
                    fecha_corte_datos = NULL, ruta_archivo = NULL
                WHERE estado = 'EN_PROCESO'
                  AND fecha_generacion < ?
                """, anterioresA);
    }
    public List<UUID> listarIdsSolicitados(int limite) {
        return jdbc.query("""
                SELECT id_exportacion
                FROM exportacion_reporte
                WHERE estado = 'SOLICITADO'
                ORDER BY fecha_solicitud
                LIMIT ?
                """,
                (rs, fila) -> rs.getObject("id_exportacion", UUID.class),
                limite);
    }
}