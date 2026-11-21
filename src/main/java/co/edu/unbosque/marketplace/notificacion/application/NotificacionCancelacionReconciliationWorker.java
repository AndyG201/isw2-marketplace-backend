package co.edu.unbosque.marketplace.notificacion.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaReservaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoReserva;
import co.edu.unbosque.marketplace.notificacion.application.DestinatariosNotificacion.DestinatariosReserva;
import co.edu.unbosque.marketplace.notificacion.application.port.CancelacionPendienteNotificacion;
import co.edu.unbosque.marketplace.notificacion.application.port.CancelacionesSinNotificarRepository;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;

@Component
public class NotificacionCancelacionReconciliationWorker {

    private final CancelacionesSinNotificarRepository cancelaciones;
    private final DestinatariosNotificacion destinatarios;
    private final NotificacionRepository notificaciones;
    private final AuditoriaReservaCommand auditoriaReservas;

    public NotificacionCancelacionReconciliationWorker(
            CancelacionesSinNotificarRepository cancelaciones,
            DestinatariosNotificacion destinatarios,
            NotificacionRepository notificaciones,
            AuditoriaReservaCommand auditoriaReservas) {
        this.cancelaciones = cancelaciones;
        this.destinatarios = destinatarios;
        this.notificaciones = notificaciones;
        this.auditoriaReservas = auditoriaReservas;
    }

    @Scheduled(fixedDelayString = "${app.notificacion.cancelacion.revision-ms:30000}")
    @Transactional
    public void registrarCancelacionesSinNotificacion() {
        for (CancelacionPendienteNotificacion cancelacion : cancelaciones.bloquearPendientes(100)) {
            Optional<DestinatariosReserva> reserva = destinatarios.buscarDestinatariosReserva(cancelacion.idReserva());
            UUID idAnfitrion = reserva.map(DestinatariosReserva::idAnfitrion).orElse(null);
            UUID idPublicacion = reserva.map(DestinatariosReserva::idPublicacion).orElse(null);
            Set<UUID> usuarios = new LinkedHashSet<>();
            if (cancelacion.idUsuario() != null) {
                usuarios.add(cancelacion.idUsuario());
            }
            if (idAnfitrion != null) {
                usuarios.add(idAnfitrion);
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
                        idPublicacion,
                        "RESERVA_CANCELADA",
                        mensaje,
                        cancelacion.fechaCancelacion(),
                        null));
            }
        }
    }

    private void conservarEvento(CancelacionPendienteNotificacion cancelacion) {
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
}
