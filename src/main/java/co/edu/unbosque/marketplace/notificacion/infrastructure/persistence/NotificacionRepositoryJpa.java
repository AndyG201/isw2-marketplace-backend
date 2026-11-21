
package co.edu.unbosque.marketplace.notificacion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;

@Repository
public interface NotificacionRepositoryJpa extends JpaRepository<Notificacion, UUID>,
        NotificacionRepository {

    List<Notificacion> findByIdUsuarioOrderByFechaCreacionDesc(
            UUID idUsuario
    );
}
