package co.edu.unbosque.marketplace.notificacion.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.notificacion.application.port.CancelacionPendienteNotificacion;
import co.edu.unbosque.marketplace.notificacion.application.port.CancelacionesSinNotificarRepository;

@Repository
public class CancelacionesSinNotificarJdbcRepository implements CancelacionesSinNotificarRepository {

    private static final String PENDIENTES = """
            SELECT c.id_cancelacion,
                   c.id_reserva,
                   c.id_usuario,
                   c.fecha_cancelacion,
                   c.motivo
            FROM public.vista_cancelacion c
            WHERE NOT EXISTS (
                SELECT 1 FROM public.notificacion n
                WHERE n.id_reserva = c.id_reserva AND n.tipo = 'RESERVA_CANCELADA'
            )
            ORDER BY c.fecha_cancelacion, c.id_cancelacion
            LIMIT ?
            FOR UPDATE OF c SKIP LOCKED
            """;

    private final JdbcTemplate jdbc;

    public CancelacionesSinNotificarJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CancelacionPendienteNotificacion> bloquearPendientes(int limite) {
        return jdbc.query(PENDIENTES, (rs, fila) -> new CancelacionPendienteNotificacion(
                rs.getObject("id_cancelacion", UUID.class),
                rs.getObject("id_reserva", UUID.class),
                rs.getObject("id_usuario", UUID.class),
                rs.getObject("fecha_cancelacion", OffsetDateTime.class),
                rs.getString("motivo")), limite);
    }
}
