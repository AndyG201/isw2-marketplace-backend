package co.edu.unbosque.marketplace.notificacion.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.infrastructure.persistence.NotificacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaRetenidaEvent;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaRetencionVencidaEvent;

@Component
public class ReservaRetencionNotificationListener {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final NotificacionRepository notificaciones;

    public ReservaRetencionNotificationListener(NotificacionRepository notificaciones) {
        this.notificaciones = notificaciones;
    }

    @EventListener
    public void alRetenerPeriodo(ReservaRetenidaEvent evento) {
        String limite = FECHA_HORA.format(evento.fechaVencimiento());
        notificaciones.save(crear(evento.idAnfitrion(), evento.idReserva(), evento.idPublicacion(),
                "RESERVA_PENDIENTE_APROBACION",
                "La solicitud de reserva " + evento.idReserva() + " requiere tu decisión antes de " + limite + "."));
        notificaciones.save(crear(evento.idHuesped(), evento.idReserva(), evento.idPublicacion(),
                "RESERVA_RETENIDA", "Tu solicitud quedó retenida hasta " + limite + ", mientras el anfitrión decide."));
    }

    @EventListener
    public void alVencerRetencion(ReservaRetencionVencidaEvent evento) {
        notificaciones.save(crear(evento.idHuesped(), evento.idReserva(), evento.idPublicacion(),
                "RESERVA_RECHAZADA_POR_VENCIMIENTO",
                "La solicitud fue rechazada automáticamente porque venció el plazo de 24 horas. Motivo: "
                        + evento.motivo() + ". El periodo fue liberado."));
    }

    private Notificacion crear(UUID idUsuario, UUID idReserva, UUID idPublicacion, String tipo, String mensaje) {
        return new Notificacion(UUID.randomUUID(), idUsuario, idReserva, idPublicacion, tipo, mensaje,
                OffsetDateTime.now(ZoneOffset.UTC), null);
    }
}
