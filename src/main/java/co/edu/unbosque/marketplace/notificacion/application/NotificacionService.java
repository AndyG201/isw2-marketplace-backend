
package co.edu.unbosque.marketplace.notificacion.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.infrastructure.persistence.NotificacionRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDesbloqueadaEvent;

@Service
public class NotificacionService {

    private static final String TIPO_PUBLICACION_DESBLOQUEADA =
            "PUBLICACION_DESBLOQUEADA";

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(
            NotificacionRepository notificacionRepository) {

        this.notificacionRepository = notificacionRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarPublicacionDesbloqueada(
            PublicacionDesbloqueadaEvent evento) {

        if (evento == null
                || evento.idAnfitrion() == null
                || evento.idPublicacion() == null
                || evento.fechaEvento() == null) {

            throw new IllegalArgumentException(
                    "Los datos del evento de desbloqueo son obligatorios."
            );
        }

        String titulo = evento.titulo() == null
                || evento.titulo().isBlank()
                ? "tu publicación"
                : "\"" + evento.titulo().trim() + "\"";

        String motivo = evento.motivo() == null
                ? "No se proporcionó información adicional."
                : evento.motivo().trim();

        String mensaje =
                "Tu publicación " + titulo
                + " ha sido desbloqueada por el administrador. "
                + "Su estado actual es PAUSADA y no está disponible "
                + "para nuevas reservas. "
                + "Motivo del desbloqueo: " + motivo;

        Notificacion notificacion = new Notificacion();

        notificacion.setIdNotificacion(UUID.randomUUID());
        notificacion.setIdUsuario(evento.idAnfitrion());
        notificacion.setIdReserva(null);
        notificacion.setIdPublicacion(evento.idPublicacion());
        notificacion.setTipo(TIPO_PUBLICACION_DESBLOQUEADA);
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(evento.fechaEvento());
        notificacion.setFechaLectura(null);

        notificacionRepository.save(notificacion);
    }
}
