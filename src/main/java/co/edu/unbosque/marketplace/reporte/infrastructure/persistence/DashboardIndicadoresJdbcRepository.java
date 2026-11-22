package co.edu.unbosque.marketplace.reporte.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion;
import co.edu.unbosque.marketplace.reporte.application.port.DashboardIndicadoresRepository;
import co.edu.unbosque.marketplace.reserva.contracts.EstadoReserva;

@Repository
public class DashboardIndicadoresJdbcRepository implements DashboardIndicadoresRepository {

    private static final String PUBLICACIONES_ACTIVAS =
            "SELECT COUNT(*) FROM public.vista_publicacion WHERE estado = 'ACTIVA'";

    private static final List<String> ESTADOS_RESERVA = EstadoReserva.nombres();

    private final JdbcTemplate jdbcTemplate;

    public DashboardIndicadoresJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public IndicadoresUsuarios consultarIndicadoresUsuarios() {
        String sql = """
                SELECT COUNT(u.id_usuario) AS total,
                       COUNT(u.id_usuario) FILTER (
                           WHERE u.estado = 'ACTIVA'
                             AND u.correo_verificado = TRUE
                             AND u.restringido = FALSE
                       ) AS activos,
                       COUNT(u.id_usuario) FILTER (
                           WHERE UPPER(BTRIM(u.rol)) = 'HUESPED'
                       ) AS huespedes,
                       COUNT(u.id_usuario) FILTER (
                           WHERE UPPER(BTRIM(u.rol)) = 'ANFITRION'
                       ) AS anfitriones
                FROM public.vista_usuario u
                """;
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new IndicadoresUsuarios(
                rs.getLong("total"),
                rs.getLong("activos"),
                rs.getLong("huespedes"),
                rs.getLong("anfitriones")));
    }

    @Override
    public long contarPublicacionesActivas() {
        Long cantidad = jdbcTemplate.queryForObject(
                PUBLICACIONES_ACTIVAS,
                Long.class);
        return cantidad == null ? 0L : cantidad;
    }

    @Override
    public Map<String, Long> contarReservasPorEstado() {
        Map<String, Long> resultados = new LinkedHashMap<>();
        ESTADOS_RESERVA.forEach(estado -> resultados.put(estado, 0L));

        jdbcTemplate.query(
                "SELECT estado, COUNT(*) AS cantidad FROM public.vista_reserva GROUP BY estado",
                rs -> {
                    String estado = rs.getString("estado");
                    if (resultados.containsKey(estado)) {
                        resultados.put(estado, rs.getLong("cantidad"));
                    }
                });
        return resultados;
    }

    @Override
    public BigDecimal calcularIngresosEstimados() {
        BigDecimal total = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(monto), 0)
                FROM public.vista_pago
                WHERE estado = 'PAGADO'
                  AND fecha_pago IS NOT NULL
                """, BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public List<CiudadDemanda> consultarCiudadesConMayorDemanda() {
        return jdbcTemplate.query("""
                SELECT COALESCE(NULLIF(BTRIM(a.ciudad), ''), 'Sin ciudad') AS ciudad,
                       COUNT(c.id_cotizacion) AS solicitudes
                FROM public.vista_cotizacion c
                JOIN public.vista_publicacion p ON p.id_publicacion = c.id_publicacion
                JOIN public.vista_alojamiento a ON a.id_alojamiento = p.id_alojamiento
                GROUP BY COALESCE(NULLIF(BTRIM(a.ciudad), ''), 'Sin ciudad')
                ORDER BY solicitudes DESC, ciudad ASC
                LIMIT 10
                """, (rs, rowNum) -> new CiudadDemanda(
                        rs.getString("ciudad"),
                        rs.getLong("solicitudes")));
    }
}
