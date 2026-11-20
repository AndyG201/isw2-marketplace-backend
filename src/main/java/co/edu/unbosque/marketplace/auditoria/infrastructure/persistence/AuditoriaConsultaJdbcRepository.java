package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;

@Repository
public class AuditoriaConsultaJdbcRepository {

    private static final String COLUMNAS = """
            id_evento, id_usuario, fecha_evento, accion, resultado,
            entidad_afectada, id_entidad_afectada, detalle::text AS detalle_json,
            id_correlacion
            """;

    private final JdbcTemplate jdbc;
    private final String url;

    public AuditoriaConsultaJdbcRepository(
            @Value("$" + "{app.auditoria.almacen.url:}") String url,
            @Value("$" + "{app.auditoria.almacen.username:}") String usuario,
            @Value("$" + "{app.auditoria.almacen.password:}") String contrasena) {
        this.url = url;
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url == null ? "" : url);
        dataSource.setUsername(usuario == null ? "" : usuario);
        dataSource.setPassword(contrasena == null ? "" : contrasena);
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public List<EventoAuditoria> consultar(
            FiltrosAuditoria filtros,
            int limite,
            long desplazamiento) {
        validarConfiguracion();
        List<Object> parametros = new ArrayList<>();
        String where = construirWhere(filtros, parametros);
        parametros.add(limite);
        parametros.add(desplazamiento);
        String sql = "SELECT " + COLUMNAS
                + " FROM public.evento_auditoria" + where
                + " ORDER BY fecha_evento DESC, id_evento DESC LIMIT ? OFFSET ?";
        return jdbc.query(sql, this::mapear, parametros.toArray());
    }

    public long contar(FiltrosAuditoria filtros) {
        validarConfiguracion();
        List<Object> parametros = new ArrayList<>();
        String where = construirWhere(filtros, parametros);
        Long total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM public.evento_auditoria" + where,
                Long.class,
                parametros.toArray());
        return total == null ? 0 : total;
    }

    public Optional<EventoAuditoria> buscar(UUID idEvento) {
        validarConfiguracion();
        List<EventoAuditoria> resultados = jdbc.query(
                "SELECT " + COLUMNAS
                        + " FROM public.evento_auditoria WHERE id_evento = ?",
                this::mapear,
                idEvento);
        return resultados.stream().findFirst();
    }

    private String construirWhere(FiltrosAuditoria filtros, List<Object> parametros) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
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
        agregarFiltro(where, parametros, "entidad_afectada", filtros.entidadAfectada());
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

    private void validarConfiguracion() {
        if (url == null || url.isBlank()) {
            throw new DataAccessResourceFailureException(
                    "El almacén de auditoría no está configurado.");
        }
    }

    private EventoAuditoria mapear(ResultSet rs, int fila) throws SQLException {
        return new EventoAuditoria(
                rs.getObject("id_evento", UUID.class),
                rs.getObject("id_usuario", UUID.class),
                rs.getObject("fecha_evento", OffsetDateTime.class),
                rs.getString("accion"),
                rs.getString("resultado"),
                rs.getString("entidad_afectada"),
                rs.getObject("id_entidad_afectada", UUID.class),
                rs.getString("detalle_json"),
                rs.getString("id_correlacion"));
    }
}