package co.edu.unbosque.marketplace.reporte.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reporte.application.port.RepartoReservasRepository;
import co.edu.unbosque.marketplace.reporte.domain.PagoParaReparto;

@Repository
public class RepartoReservasJdbcRepository implements RepartoReservasRepository {

    /**
     * Un registro por pago aprobado. La devolución sale de la cancelación de la reserva (si la hubo) y los porcentajes
     * son los de la tarifa con la que se cotizó, no los de la tarifa vigente hoy.
     */
    private static final String PAGOS_APROBADOS = """
            SELECT r.id_reserva,
                   a.id_anfitrion,
                   p.id_publicacion,
                   p.titulo,
                   pg.monto,
                   c.precio_noche_aplicado,
                   (c.fecha_salida - c.fecha_entrada) AS noches,
                   t.porcentaje_servicio,
                   t.porcentaje_descuento,
                   COALESCE(ca.valor_devolucion, 0) AS devolucion
            FROM public.pago pg
            JOIN public.reserva r ON r.id_reserva = pg.id_reserva
            JOIN public.cotizacion c ON c.id_cotizacion = r.id_cotizacion
            JOIN public.configuracion_tarifa t ON t.id_configuracion = c.id_configuracion
            JOIN public.publicacion p ON p.id_publicacion = c.id_publicacion
            JOIN public.alojamiento a ON a.id_alojamiento = p.id_alojamiento
            LEFT JOIN public.cancelacion ca ON ca.id_reserva = r.id_reserva
            WHERE pg.estado = 'PAGADO'
              AND pg.fecha_pago IS NOT NULL
            """;

    private static final String SOLO_DEL_ANFITRION = " AND a.id_anfitrion = ?";
    private static final String ORDEN = " ORDER BY pg.fecha_pago, r.id_reserva";

    private static final RowMapper<PagoParaReparto> MAPEADOR = (rs, fila) -> new PagoParaReparto(
            rs.getObject("id_reserva", UUID.class),
            rs.getObject("id_anfitrion", UUID.class),
            rs.getObject("id_publicacion", UUID.class),
            rs.getString("titulo"),
            rs.getBigDecimal("monto"),
            rs.getBigDecimal("precio_noche_aplicado"),
            rs.getLong("noches"),
            rs.getBigDecimal("porcentaje_servicio"),
            rs.getBigDecimal("porcentaje_descuento"),
            rs.getBigDecimal("devolucion"));

    private final JdbcTemplate jdbc;

    public RepartoReservasJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<PagoParaReparto> pagosAprobados() {
        return jdbc.query(PAGOS_APROBADOS + ORDEN, MAPEADOR);
    }

    @Override
    public List<PagoParaReparto> pagosAprobadosDelAnfitrion(UUID idAnfitrion) {
        return jdbc.query(PAGOS_APROBADOS + SOLO_DEL_ANFITRION + ORDEN, MAPEADOR, idAnfitrion);
    }
}
