package co.edu.unbosque.marketplace.notificacion.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaPublicacionCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaReservaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoPublicacion;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoReserva;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;

@Component
public class NotificacionEstadoCambioHandler {

    private static final String RESERVA = "RESERVA";
    private static final String PUBLICACION = "PUBLICACION";
    private static final String EN_REVISION = "PENDIENTE_REVISION";
    private static final String BORRADOR = "BORRADOR";
    private static final List<String> DECISIONES_RESERVA_NOTIFICADAS = List.of("PENDIENTE_PAGO", "RECHAZADA");

    private final NotificacionRepository notificaciones;
    private final DestinatariosNotificacion destinatarios;
    private final AuditoriaReservaCommand auditoriaReservas;
    private final AuditoriaPublicacionCommand auditoriaPublicaciones;

    public NotificacionEstadoCambioHandler(
            NotificacionRepository notificaciones,
            DestinatariosNotificacion destinatarios,
            AuditoriaReservaCommand auditoriaReservas,
            AuditoriaPublicacionCommand auditoriaPublicaciones) {
        this.notificaciones = notificaciones;
        this.destinatarios = destinatarios;
        this.auditoriaReservas = auditoriaReservas;
        this.auditoriaPublicaciones = auditoriaPublicaciones;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCambio(EstadoMarketplaceCambioEvent evento) {
        if (!esEventoValido(evento) || yaLoNotificaUnListenerExistente(evento)) {
            return;
        }
        if (RESERVA.equals(evento.entidad())) {
            registrarCambioReserva(evento);
        } else if (PUBLICACION.equals(evento.entidad())) {
            registrarCambioPublicacion(evento);
        }
    }

    private void registrarCambioReserva(EstadoMarketplaceCambioEvent evento) {
        var contexto = destinatarios.buscarDestinatariosReserva(evento.idEntidad());
        if (contexto.isEmpty()) {
            conservarEventoReserva(evento);
            return;
        }

        var datos = contexto.get();
        Set<UUID> usuarios = new LinkedHashSet<>();
        if (datos.idHuesped() != null) {
            usuarios.add(datos.idHuesped());
        }
        if (datos.idAnfitrion() != null) {
            usuarios.add(datos.idAnfitrion());
        }
        if (usuarios.isEmpty()) {
            conservarEventoReserva(evento);
            return;
        }

        for (UUID idUsuario : usuarios) {
            notificaciones.save(new Notificacion(
                    UUID.randomUUID(),
                    idUsuario,
                    evento.idEntidad(),
                    datos.idPublicacion(),
                    "RESERVA_ESTADO_CAMBIADO",
                    mensajeReserva(idUsuario, datos.idHuesped(), evento),
                    evento.fechaEvento(),
                    null));
        }
    }

    private void registrarCambioPublicacion(EstadoMarketplaceCambioEvent evento) {
        var anfitrion = destinatarios.buscarAnfitrionPublicacion(evento.idEntidad());
        if (anfitrion.isEmpty()) {
            conservarEventoPublicacion(evento);
            return;
        }
        notificaciones.save(new Notificacion(
                UUID.randomUUID(),
                anfitrion.get(),
                null,
                evento.idEntidad(),
                "PUBLICACION_ESTADO_CAMBIADO",
                "El estado de tu publicación cambió de " + evento.estadoAnterior()
                        + " a " + evento.estadoNuevo() + ".",
                evento.fechaEvento(),
                null));
        if (EN_REVISION.equals(evento.estadoNuevo())) {
            avisarAdministradores(evento);
        }
    }

    private void avisarAdministradores(EstadoMarketplaceCambioEvent evento) {
        String mensaje = BORRADOR.equals(evento.estadoAnterior())
                ? "Hay una publicación nueva esperando tu revisión."
                : "Un anfitrión editó una publicación aprobada y espera tu revisión.";
        for (UUID idAdministrador : destinatarios.buscarAdministradoresActivos()) {
            notificaciones.save(new Notificacion(
                    UUID.randomUUID(),
                    idAdministrador,
                    null,
                    evento.idEntidad(),
                    "PUBLICACION_POR_REVISAR",
                    mensaje,
                    evento.fechaEvento(),
                    null));
        }
    }

    private String mensajeReserva(UUID idUsuario, UUID idHuesped, EstadoMarketplaceCambioEvent evento) {
        String destinatario = idUsuario.equals(idHuesped) ? "Tu reserva" : "Una reserva de tu alojamiento";
        return destinatario + " cambió de estado: " + evento.estadoAnterior() + " → " + evento.estadoNuevo() + ".";
    }

    private boolean yaLoNotificaUnListenerExistente(EstadoMarketplaceCambioEvent evento) {
        if (RESERVA.equals(evento.entidad())
                && "PENDIENTE_APROBACION".equals(evento.estadoAnterior())
                && DECISIONES_RESERVA_NOTIFICADAS.contains(evento.estadoNuevo())) {
            return true;
        }
        return PUBLICACION.equals(evento.entidad())
                && "BLOQUEADA".equals(evento.estadoAnterior())
                && "PAUSADA".equals(evento.estadoNuevo());
    }

    private boolean esEventoValido(EstadoMarketplaceCambioEvent evento) {
        return evento != null
                && (RESERVA.equals(evento.entidad()) || PUBLICACION.equals(evento.entidad()))
                && evento.idEntidad() != null
                && evento.estadoAnterior() != null
                && evento.estadoNuevo() != null
                && evento.fechaEvento() != null;
    }

    private void conservarEventoReserva(EstadoMarketplaceCambioEvent evento) {
        String detalle = "{\"estadoAnterior\":\"" + evento.estadoAnterior()
                + "\",\"estadoNuevo\":\"" + evento.estadoNuevo() + "\"}";
        auditoriaReservas.registrarEvento(new RegistrarEventoReserva(
                null,
                evento.idEntidad(),
                evento.fechaEvento(),
                "NOTIFICACION_SIN_DESTINATARIO",
                "PENDIENTE",
                detalle));
    }

    private void conservarEventoPublicacion(EstadoMarketplaceCambioEvent evento) {
        String detalle = "{\"estadoAnterior\":\"" + evento.estadoAnterior()
                + "\",\"estadoNuevo\":\"" + evento.estadoNuevo() + "\"}";
        auditoriaPublicaciones.registrarEvento(new RegistrarEventoPublicacion(
                null,
                evento.idEntidad(),
                evento.fechaEvento(),
                "NOTIFICACION_SIN_DESTINATARIO",
                "PENDIENTE",
                detalle));
    }
}
