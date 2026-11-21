package co.edu.unbosque.marketplace.notificacion.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;

public interface NotificacionRepository extends RepositorioBasico<Notificacion, UUID> {
    List<Notificacion> findByIdUsuarioOrderByFechaCreacionDesc(UUID idUsuario);
}
