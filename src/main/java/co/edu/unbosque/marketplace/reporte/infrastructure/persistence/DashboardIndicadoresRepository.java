package co.edu.unbosque.marketplace.reporte.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardIndicadoresRepository {

    private static final List<String> ESTADOS_RESERVA = List.of(
            "PENDIENTE_PAGO",
            "PENDIENTE_APROBACION",
            "CONFIRMADA",
            "RECHAZADA",
            "CANCELADA",
            "COMPLETADA",
            "EXPIRADA");

    private final JdbcTemplate jdbcTemplate;

    public DashboardIndicadoresRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public IndicadoresUsuarios consultarIndicadoresUsuarios() {
        String sql = """
                SELECT COUNT(u.id_usuario) AS total,
                       COUNT(u.id_usuario) FILTER (
                           WHERE u.estado = 'ACTIVA'
                             AND u.correo_verificado = TRUE
                             AND u.restringido = FALSE
                       ) AS activos,
                       COUNT(u.id_usuario) FILTER (
                           WHERE UPPER(BTRIM(r.nombre)) = 'HUESPED'
                       ) AS huespedes,
                       COUNT(u.id_usuario) FILTER (
                           WHERE UPPER(BTRIM(r.nombre)) = 'ANFITRION'
                       ) AS anfitriones
                FROM usuario u
                LEFT JOIN rol r ON r.id_rol = u.id_rol
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new IndicadoresUsuarios(
                rs.getLong("total"),
                rs.getLong("activos"),
                rs.getLong("huespedes"),
                rs.getLong("anfitriones")));
    }

    public long contarPublicacionesActivas() {
        Long cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM publicacion WHERE estado = 'ACTIVA'",
                Long.class);
        return cantidad == null ? 0L : cantidad;
    }

    public Map<String, Long> contarReservasPorEstado() {
        Map<String, Long> resultados = new LinkedHashMap<>();
        ESTADOS_RESERVA.forEach(estado -> resultados.put(estado, 0L));

        jdbcTemplate.query(
                "SELECT estado, COUNT(*) AS cantidad FROM reserva GROUP BY estado",
                rs -> {
                    String estado = rs.getString("estado");
                    if (resultados.containsKey(estado)) {
                        resultados.put(estado, rs.getLong("cantidad"));
                    }
                });
        return resultados;
    }

    public BigDecimal calcularIngresosEstimados() {
        BigDecimal total = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(monto), 0)
                FROM pago
                WHERE estado = 'PAGADO'
                  AND fecha_pago IS NOT NULL
                """, BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    public List<CiudadDemanda> consultarCiudadesConMayorDemanda() {
        return jdbcTemplate.query("""
                SELECT COALESCE(NULLIF(BTRIM(a.ciudad), ''), 'Sin ciudad') AS ciudad,
                       COUNT(c.id_cotizacion) AS solicitudes
                FROM cotizacion c
                JOIN publicacion p ON p.id_publicacion = c.id_publicacion
                JOIN alojamiento a ON a.id_alojamiento = p.id_alojamiento
                GROUP BY COALESCE(NULLIF(BTRIM(a.ciudad), ''), 'Sin ciudad')
                ORDER BY solicitudes DESC, ciudad ASC
                LIMIT 10
                """, (rs, rowNum) -> new CiudadDemanda(
                        rs.getString("ciudad"),
                        rs.getLong("solicitudes")));
    }

    public record IndicadoresUsuarios(
            long total,
            long activos,
            long huespedes,
            long anfitriones) {
    }

    public record CiudadDemanda(
            String ciudad,
            long solicitudesCotizacion) {
    }
}
