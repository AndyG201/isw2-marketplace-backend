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

import co.edu.unbosque.marketplace.reserva.application.port.ReservaAnfitrionDatos;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaAnfitrionRepository;

@Repository
public class ReservaAnfitrionJdbcRepository implements ReservaAnfitrionRepository {

    private static final String SELECT_BASE = """
            SELECT r.id_reserva AS "idReserva",
                   a.id_alojamiento AS "idAlojamiento",
                   p.titulo AS "alojamiento",
                   h.id_usuario AS "idHuesped",
                   concat_ws(' ', NULLIF(trim(h.nombre), ''), NULLIF(trim(h.apellido), '')) AS "huesped",
                   c.fecha_entrada AS "fechaEntrada",
                   c.fecha_salida AS "fechaSalida",
                   c.monto_total AS "montoTotal",
                   r.estado AS "estado",
                   r.retencion_hasta AS "retencionHasta"
            FROM public.reserva r
            INNER JOIN public.vista_cotizacion c
                ON c.id_cotizacion = r.id_cotizacion
            INNER JOIN public.vista_publicacion p
                ON p.id_publicacion = c.id_publicacion
            INNER JOIN public.vista_alojamiento a
                ON a.id_alojamiento = p.id_alojamiento
            INNER JOIN public.vista_usuario h
                ON h.id_usuario = r.id_huesped
            """;

    private final JdbcTemplate jdbc;
    private final RowMapper<ReservaAnfitrionDatos> mapper = this::mapear;

    public ReservaAnfitrionJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ReservaAnfitrionDatos> consultarPorAnfitrion(UUID idAnfitrion) {
        return jdbc.query(
                SELECT_BASE + """
                        WHERE a.id_anfitrion = ?
                        ORDER BY c.fecha_entrada DESC, r.fecha_solicitud DESC, r.id_reserva
                        """,
                mapper,
                idAnfitrion);
    }

    @Override
    public Optional<ReservaAnfitrionDatos> consultarReservaPorAnfitrion(
            UUID idAnfitrion,
            UUID idReserva) {
        return jdbc.query(
                        SELECT_BASE + " WHERE a.id_anfitrion = ? AND r.id_reserva = ?",
                        mapper,
                        idAnfitrion,
                        idReserva)
                .stream()
                .findFirst();
    }

    private ReservaAnfitrionDatos mapear(ResultSet resultado, int fila) throws SQLException {
        return new ReservaAnfitrionDatos(
                resultado.getObject("idReserva", UUID.class),
                resultado.getObject("idAlojamiento", UUID.class),
                resultado.getString("alojamiento"),
                resultado.getObject("idHuesped", UUID.class),
                resultado.getString("huesped"),
                resultado.getObject("fechaEntrada", LocalDate.class),
                resultado.getObject("fechaSalida", LocalDate.class),
                resultado.getBigDecimal("montoTotal"),
                resultado.getString("estado"),
                resultado.getObject("retencionHasta", OffsetDateTime.class));
    }
}
