package co.edu.unbosque.marketplace.notificacion.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaDecisionEvent;

@Component
public class ReservaDecisionNotificationListener {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final NotificacionRepository notificaciones;

    public ReservaDecisionNotificationListener(NotificacionRepository notificaciones) {
        this.notificaciones = notificaciones;
    }

    @EventListener
    public void alDecidirSolicitud(ReservaDecisionEvent evento) {
        String tipo;
        String mensaje;

        switch (evento.decision()) {
            case "APROBADA_PENDIENTE_PAGO" -> {
                tipo = "RESERVA_APROBADA_PENDIENTE_PAGO";
                mensaje = "El anfitrión aprobó tu solicitud. Puedes completar el pago antes de "
                        + FORMATO_FECHA.format(evento.retencionHasta()) + ".";
            }
            case "RECHAZADA" -> {
                tipo = "RESERVA_RECHAZADA";
                mensaje = "El anfitrión rechazó tu solicitud de reserva. El periodo fue liberado.";
            }
            case "VENCIDA" -> {
                tipo = "RESERVA_RECHAZADA_POR_VENCIMIENTO";
                mensaje = "La solicitud fue rechazada automáticamente al vencer el plazo de 24 horas. "
                        + "El periodo fue liberado.";
            }
            default -> {
                return;
            }
        }

        notificaciones.save(new Notificacion(
                UUID.randomUUID(),
                evento.idHuesped(),
                evento.idReserva(),
                evento.idPublicacion(),
                tipo,
                mensaje,
                OffsetDateTime.now(ZoneOffset.UTC),
                null));
    }
}