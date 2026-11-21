
package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;

/**
 * Repositorio de consulta del índice de auditoría almacenado
 * en la base de datos principal de Marketplace.
 *
 * Los documentos JSON originales permanecen en Backblaze B2.
 */
@Repository
public class AuditoriaConsultaJdbcRepository {

    private static final String COLUMNAS = """
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
            version_objeto
            """;

    private final JdbcTemplate jdbc;

    public AuditoriaConsultaJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<EventoAuditoriaIndice> consultar(
            FiltrosAuditoria filtros,
            int limite,
            long desplazamiento) {

        validarPaginacion(limite, desplazamiento);

        List<Object> parametros = new ArrayList<>();
        String where = construirWhere(filtros, parametros);

        parametros.add(limite);
        parametros.add(desplazamiento);

        String sql = "SELECT " + COLUMNAS
                + " FROM public.evento_auditoria"
                + where
                + " ORDER BY fecha_evento DESC, id_evento DESC"
                + " LIMIT ? OFFSET ?";

        return jdbc.query(sql, this::mapear, parametros.toArray());
    }

    public long contar(FiltrosAuditoria filtros) {

        List<Object> parametros = new ArrayList<>();
        String where = construirWhere(filtros, parametros);

        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM public.evento_auditoria" + where,
                Long.class,
                parametros.toArray()
        );

        return total == null ? 0 : total;
    }

    public Optional<EventoAuditoriaIndice> buscar(UUID idEvento) {

        if (idEvento == null) {
            return Optional.empty();
        }

        List<EventoAuditoriaIndice> resultados = jdbc.query(
                "SELECT " + COLUMNAS
                        + " FROM public.evento_auditoria"
                        + " WHERE id_evento = ?",
                this::mapear,
                idEvento
        );

        return resultados.stream().findFirst();
    }

    private String construirWhere(
            FiltrosAuditoria filtros,
            List<Object> parametros) {

        StringBuilder where = new StringBuilder(" WHERE 1 = 1");

        if (filtros == null) {
            return where.toString();
        }

        if (filtros.idUsuario() != null) {
            where.append(" AND id_usuario = ?");
            parametros.add(filtros.idUsuario());
        }

        if (filtros.desde() != null) {
            where.append(" AND fecha_evento >= ?");
            parametros.add(filtros.desde());
        }

        if (filtros.hasta() != null) {
            where.append(" AND fecha_evento <= ?");
            parametros.add(filtros.hasta());
        }

        agregarFiltro(where, parametros, "accion", filtros.accion());
        agregarFiltro(where, parametros, "resultado", filtros.resultado());
        agregarFiltro(
                where,
                parametros,
                "entidad_afectada",
                filtros.entidadAfectada()
        );

        return where.toString();
    }

    private void agregarFiltro(
            StringBuilder where,
            List<Object> parametros,
            String columna,
            String valor) {

        if (valor != null && !valor.isBlank()) {
            where.append(" AND ").append(columna).append(" = ?");
            parametros.add(valor.trim());
        }
    }

    private void validarPaginacion(int limite, long desplazamiento) {

        if (limite <= 0 || desplazamiento < 0) {
            throw new IllegalArgumentException(
                    "Los parámetros de paginación no son válidos.");
        }
    }

    private EventoAuditoriaIndice mapear(
            ResultSet rs,
            int fila) throws SQLException {

        return new EventoAuditoriaIndice(
                rs.getObject("id_evento", UUID.class),
                rs.getObject("id_usuario", UUID.class),
                rs.getObject("fecha_evento", OffsetDateTime.class),
                rs.getString("accion"),
                rs.getString("resultado"),
                rs.getString("entidad_afectada"),
                rs.getObject("id_entidad_afectada", UUID.class),
                rs.getString("id_correlacion"),
                rs.getString("clave_objeto"),
                rs.getString("sha256"),
                rs.getString("version_objeto")
        );
    }
}
