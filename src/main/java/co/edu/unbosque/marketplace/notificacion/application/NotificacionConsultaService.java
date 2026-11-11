
package co.edu.unbosque.marketplace.notificacion.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.notificacion.api.response.NotificacionResponse;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.infrastructure.persistence.NotificacionRepository;

@Service
public class NotificacionConsultaService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionConsultaService(
            NotificacionRepository notificacionRepository) {

        this.notificacionRepository = notificacionRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> consultarMisNotificaciones(
            UUID idUsuario) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al usuario."
            );
        }

        return notificacionRepository
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    private NotificacionResponse convertirRespuesta(
            Notificacion notificacion) {

        return new NotificacionResponse(
                notificacion.getIdNotificacion(),
                notificacion.getIdUsuario(),
                notificacion.getIdReserva(),
                notificacion.getIdPublicacion(),
                notificacion.getTipo(),
                notificacion.getMensaje(),
                notificacion.getFechaCreacion(),
                notificacion.getFechaLectura()
        );
    }
}
