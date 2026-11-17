package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ReservaAnfitrionRepository {

    private static final String SELECT_BASE = """
            SELECT r.id_reserva AS "idReserva",
                   a.id_alojamiento AS "idAlojamiento",
                   p.titulo AS "alojamiento",
                   h.id_usuario AS "idHuesped",
                   concat_ws(' ', NULLIF(trim(h.nombre), ''), NULLIF(trim(h.apellido), '')) AS "huesped",
                   c.fecha_entrada AS "fechaEntrada",
                   c.fecha_salida AS "fechaSalida",
                   ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
                    + c.tarifa_limpieza
                    + ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
                       * ct.porcentaje_servicio / 100)
                    - ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
                       * ct.porcentaje_descuento / 100)
                    + COALESCE((SELECT SUM(ca.cantidad * ca.precio_aplicado)
                                FROM public.cotizacion_adicional ca
                                WHERE ca.id_cotizacion = c.id_cotizacion), 0))
                       AS "montoTotal",
                   r.estado AS "estado"
            FROM public.reserva r
            INNER JOIN public.cotizacion c
                ON c.id_cotizacion = r.id_cotizacion
            INNER JOIN public.publicacion p
                ON p.id_publicacion = c.id_publicacion
            INNER JOIN public.alojamiento a
                ON a.id_alojamiento = p.id_alojamiento
            INNER JOIN public.configuracion_tarifa ct
                ON ct.id_configuracion = c.id_configuracion
            INNER JOIN public.usuario h
                ON h.id_usuario = r.id_huesped
            """;

    private final JdbcTemplate jdbc;
    private final RowMapper<ReservaAnfitrionDatos> mapper = this::mapear;

    public ReservaAnfitrionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ReservaAnfitrionDatos> consultarPorAnfitrion(UUID idAnfitrion) {
        return jdbc.query(
                SELECT_BASE + """
                        WHERE a.id_anfitrion = ?
                        ORDER BY c.fecha_entrada DESC, r.fecha_solicitud DESC, r.id_reserva
                        """,
                mapper,
                idAnfitrion);
    }

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
                resultado.getString("estado"));
    }
}
