package co.edu.unbosque.marketplace.notificacion.application;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaReservaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoReserva;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;

@Component
public class NotificacionCancelacionReconciliationWorker {

    private static final String CONSULTA = """
            SELECT c.id_cancelacion AS "idCancelacion",
                   c.id_reserva AS "idReserva",
                   c.id_usuario AS "idUsuario",
                   c.fecha_cancelacion AS "fechaCancelacion",
                   c.motivo AS "motivo",
                   c.id_cancelacion AS "correlacion",
                   co.id_publicacion AS "idPublicacion",
                   a.id_anfitrion AS "idAnfitrion"
            FROM public.cancelacion c
            JOIN public.reserva r ON r.id_reserva = c.id_reserva
            LEFT JOIN public.cotizacion co ON co.id_cotizacion = r.id_cotizacion
            LEFT JOIN public.publicacion p ON p.id_publicacion = co.id_publicacion
            LEFT JOIN public.alojamiento a ON a.id_alojamiento = p.id_alojamiento
            WHERE NOT EXISTS (
                SELECT 1 FROM public.notificacion n
                WHERE n.id_reserva = c.id_reserva AND n.tipo = 'RESERVA_CANCELADA'
            )
            ORDER BY c.fecha_cancelacion, c.id_cancelacion
            LIMIT 100
            FOR UPDATE OF c SKIP LOCKED
            """;

    private final JdbcTemplate jdbc;
    private final NotificacionRepository notificaciones;
    private final AuditoriaReservaCommand auditoriaReservas;
    private final RowMapper<CancelacionPendiente> mapper = this::mapear;

    public NotificacionCancelacionReconciliationWorker(
            JdbcTemplate jdbc,
            NotificacionRepository notificaciones,
            AuditoriaReservaCommand auditoriaReservas) {
        this.jdbc = jdbc;
        this.notificaciones = notificaciones;
        this.auditoriaReservas = auditoriaReservas;
    }

    @Scheduled(fixedDelayString = "${app.notificacion.cancelacion.revision-ms:30000}")
    @Transactional
    public void registrarCancelacionesSinNotificacion() {
        List<CancelacionPendiente> pendientes = jdbc.query(CONSULTA, mapper);
        for (CancelacionPendiente cancelacion : pendientes) {
            Set<UUID> usuarios = new LinkedHashSet<>();
            if (cancelacion.idUsuario() != null) {
                usuarios.add(cancelacion.idUsuario());
            }
            if (cancelacion.idAnfitrion() != null) {
                usuarios.add(cancelacion.idAnfitrion());
            }
            if (usuarios.isEmpty()) {
                conservarEvento(cancelacion);
                continue;
            }

            for (UUID idUsuario : usuarios) {
                String mensaje = idUsuario.equals(cancelacion.idUsuario())
                        ? "Tu reserva fue cancelada. Motivo: " + cancelacion.motivo()
                        : "Una reserva de tu alojamiento fue cancelada.";
                notificaciones.save(new Notificacion(
                        UUID.randomUUID(),
                        idUsuario,
                        cancelacion.idReserva(),
                        cancelacion.idPublicacion(),
                        "RESERVA_CANCELADA",
                        mensaje,
                        cancelacion.fechaCancelacion(),
                        null));
            }
        }
    }

    private void conservarEvento(CancelacionPendiente cancelacion) {
        String detalle = "{\"idCancelacion\":\"" + cancelacion.idCancelacion()
                + "\",\"motivo\":\"SIN_DESTINATARIO\"}";
        auditoriaReservas.registrarEvento(new RegistrarEventoReserva(
                null,
                cancelacion.idReserva(),
                OffsetDateTime.now(ZoneOffset.UTC),
                "NOTIFICACION_SIN_DESTINATARIO",
                "PENDIENTE",
                detalle));
    }

    private CancelacionPendiente mapear(ResultSet rs, int fila) throws SQLException {
        return new CancelacionPendiente(
                rs.getObject("idCancelacion", UUID.class),
                rs.getObject("idReserva", UUID.class),
                rs.getObject("idUsuario", UUID.class),
                rs.getObject("fechaCancelacion", OffsetDateTime.class),
                rs.getString("motivo"),
                rs.getObject("idPublicacion", UUID.class),
                rs.getObject("idAnfitrion", UUID.class));
    }

    private record CancelacionPendiente(
            UUID idCancelacion,
            UUID idReserva,
            UUID idUsuario,
            OffsetDateTime fechaCancelacion,
            String motivo,
            UUID idPublicacion,
            UUID idAnfitrion) {
    }
}
