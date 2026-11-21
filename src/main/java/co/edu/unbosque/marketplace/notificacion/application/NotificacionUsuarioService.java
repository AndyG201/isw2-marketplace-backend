package co.edu.unbosque.marketplace.notificacion.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.notificacion.contracts.CrearNotificacionUsuario;
import co.edu.unbosque.marketplace.notificacion.contracts.NotificacionUsuarioCommand;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.infrastructure.persistence.NotificacionRepository;

@Service
public class NotificacionUsuarioService implements NotificacionUsuarioCommand {

    private final NotificacionRepository notificaciones;

    public NotificacionUsuarioService(NotificacionRepository notificaciones) {
        this.notificaciones = notificaciones;
    }

    @Override
    @Transactional
    public void crearNotificacion(CrearNotificacionUsuario solicitud) {
        if (solicitud == null
                || solicitud.idUsuario() == null
                || solicitud.tipo() == null
                || solicitud.tipo().isBlank()
                || solicitud.mensaje() == null
                || solicitud.mensaje().isBlank()
                || solicitud.fechaCreacion() == null) {
            throw new IllegalArgumentException(
                    "Los datos de la notificación son obligatorios.");
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setIdNotificacion(UUID.randomUUID());
        notificacion.setIdUsuario(solicitud.idUsuario());
        notificacion.setIdReserva(null);
        notificacion.setIdPublicacion(null);
        notificacion.setTipo(solicitud.tipo());
        notificacion.setMensaje(solicitud.mensaje());
        notificacion.setFechaCreacion(solicitud.fechaCreacion());
        notificacion.setFechaLectura(null);

        notificaciones.save(notificacion);
    }
}