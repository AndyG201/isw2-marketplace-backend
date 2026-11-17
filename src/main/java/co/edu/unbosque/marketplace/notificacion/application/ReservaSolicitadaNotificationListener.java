package co.edu.unbosque.marketplace.notificacion.application;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.infrastructure.persistence.NotificacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaSolicitadaEvent;

@Component
public class ReservaSolicitadaNotificationListener {

    private static final String TIPO_RESERVA_SOLICITADA =
            "RESERVA_SOLICITADA";

    private final NotificacionRepository notificacionRepository;

    public ReservaSolicitadaNotificationListener(
            NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @EventListener
    public void alSolicitarReserva(ReservaSolicitadaEvent evento) {
        if (evento == null
                || evento.idAnfitrion() == null
                || evento.idReserva() == null
                || evento.idPublicacion() == null
                || evento.fechaEvento() == null) {
            throw new IllegalArgumentException(
                    "Los datos del evento de reserva son obligatorios.");
        }

        Notificacion notificacion = new Notificacion(
                UUID.randomUUID(),
                evento.idAnfitrion(),
                evento.idReserva(),
                evento.idPublicacion(),
                TIPO_RESERVA_SOLICITADA,
                "Recibiste una nueva solicitud de reserva que requiere tu aprobación.",
                evento.fechaEvento(),
                null);

        notificacionRepository.save(notificacion);
    }
}