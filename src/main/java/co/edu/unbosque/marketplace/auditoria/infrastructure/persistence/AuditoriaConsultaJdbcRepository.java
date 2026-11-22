
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

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.auditoria.application.port.AuditoriaConsultaRepository;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;

/**
 * Repositorio de consulta del índice de auditoría almacenado
 * en la base de datos principal de Marketplace.
 *
 * Los documentos JSON originales permanecen en Backblaze B2.
 */
@Repository
public class AuditoriaConsultaJdbcRepository implements AuditoriaConsultaRepository {

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

    /**
     * Cada filtro se escribe como "si no viene, no filtra". Así la consulta es siempre la misma
     * (no se arma texto SQL con piezas variables) y los valores viajan solo como parámetros.
     */
    private static final String FILTROS = """
             WHERE (CAST(? AS uuid) IS NULL OR id_usuario = CAST(? AS uuid))
               AND (CAST(? AS timestamptz) IS NULL OR fecha_evento >= CAST(? AS timestamptz))
               AND (CAST(? AS timestamptz) IS NULL OR fecha_evento <= CAST(? AS timestamptz))
               AND (CAST(? AS text) IS NULL OR accion = CAST(? AS text))
               AND (CAST(? AS text) IS NULL OR resultado = CAST(? AS text))
               AND (CAST(? AS text) IS NULL OR entidad_afectada = CAST(? AS text))
            """;

    private static final String CONSULTA = "SELECT " + COLUMNAS
            + " FROM public.evento_auditoria"
            + FILTROS
            + " ORDER BY fecha_evento DESC, id_evento DESC"
            + " LIMIT ? OFFSET ?";

    private static final String CONTEO = "SELECT COUNT(*) FROM public.evento_auditoria" + FILTROS;

    private final JdbcTemplate jdbc;

    public AuditoriaConsultaJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<EventoAuditoriaIndice> consultar(
            FiltrosAuditoria filtros,
            int limite,
            long desplazamiento) {

        validarPaginacion(limite, desplazamiento);

        List<Object> parametros = valoresDeLosFiltros(filtros);
        parametros.add(limite);
        parametros.add(desplazamiento);

        return jdbc.query(CONSULTA, this::mapear, parametros.toArray());
    }

    @Override
    public long contar(FiltrosAuditoria filtros) {

        Long total = jdbc.queryForObject(
                CONTEO,
                Long.class,
                valoresDeLosFiltros(filtros).toArray()
        );

        return total == null ? 0 : total;
    }

    @Override
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

    /**
     * Los valores de los filtros en el mismo orden que {@link #FILTROS}. Cada uno va dos veces:
     * una para preguntar si viene y otra para comparar.
     */
    private List<Object> valoresDeLosFiltros(FiltrosAuditoria filtros) {

        List<Object> valores = new ArrayList<>();

        if (filtros == null) {
            filtros = new FiltrosAuditoria(null, null, null, null, null, null);
        }

        agregarDos(valores, filtros.idUsuario() == null ? null : filtros.idUsuario().toString());
        agregarDos(valores, filtros.desde());
        agregarDos(valores, filtros.hasta());
        agregarDos(valores, textoONulo(filtros.accion()));
        agregarDos(valores, textoONulo(filtros.resultado()));
        agregarDos(valores, textoONulo(filtros.entidadAfectada()));

        return valores;
    }

    private void agregarDos(List<Object> valores, Object valor) {
        valores.add(valor);
        valores.add(valor);
    }

    private String textoONulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void validarPaginacion(int limite, long desplazamiento) {

        if (limite <= 0 || desplazamiento < 0) {
            throw new DatosInvalidosException(
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
