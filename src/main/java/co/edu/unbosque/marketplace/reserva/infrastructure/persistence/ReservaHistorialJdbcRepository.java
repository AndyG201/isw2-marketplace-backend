package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.application.port.ReservaHistorialDatos;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaHistorialRepository;

@Repository
public class ReservaHistorialJdbcRepository implements ReservaHistorialRepository {

    private static final String SELECT_BASE = """
            SELECT r.id_reserva AS "idReserva",
                   a.id_alojamiento AS "idAlojamiento",
                   p.titulo AS "alojamiento",
                   c.fecha_entrada AS "fechaEntrada",
                   c.fecha_salida AS "fechaSalida",
                   c.monto_total AS "montoTotal",
                   r.estado AS "estado",
                   r.fecha_solicitud AS "fechaSolicitud",
                   r.retencion_hasta AS "retencionHasta",
                   EXISTS (SELECT 1
                           FROM public.vista_resena rs
                           WHERE rs.id_reserva = r.id_reserva) AS "resenada"
            FROM public.reserva r
            INNER JOIN public.vista_cotizacion c
                ON c.id_cotizacion = r.id_cotizacion
            INNER JOIN public.vista_publicacion p
                ON p.id_publicacion = c.id_publicacion
            INNER JOIN public.vista_alojamiento a
                ON a.id_alojamiento = p.id_alojamiento
            """;

    private final JdbcTemplate jdbc;
    private final RowMapper<ReservaHistorialDatos> mapper = this::mapear;

    public ReservaHistorialJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ReservaHistorialDatos> consultarPorHuesped(UUID idHuesped) {
        return jdbc.query(
                SELECT_BASE + """
                        WHERE r.id_huesped = ?
                        ORDER BY r.fecha_solicitud DESC, r.id_reserva
                        """,
                mapper,
                idHuesped);
    }

    @Override
    public Optional<ReservaHistorialDatos> consultarPorId(UUID idReserva) {
        return jdbc.query(
                        SELECT_BASE + " WHERE r.id_reserva = ?",
                        mapper,
                        idReserva)
                .stream()
                .findFirst();
    }

    private ReservaHistorialDatos mapear(ResultSet resultado, int fila) throws SQLException {
        return new ReservaHistorialDatos(
                resultado.getObject("idReserva", UUID.class),
                resultado.getObject("idAlojamiento", UUID.class),
                resultado.getString("alojamiento"),
                resultado.getObject("fechaEntrada", LocalDate.class),
                resultado.getObject("fechaSalida", LocalDate.class),
                resultado.getBigDecimal("montoTotal"),
                resultado.getString("estado"),
                resultado.getObject("fechaSolicitud", OffsetDateTime.class),
                resultado.getObject("retencionHasta", OffsetDateTime.class),
                resultado.getBoolean("resenada"));
    }
}