
package co.edu.unbosque.marketplace.notificacion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;

@Repository
public interface NotificacionRepository
        extends JpaRepository<Notificacion, UUID> {

    List<Notificacion> findByIdUsuarioOrderByFechaCreacionDesc(
            UUID idUsuario
    );
}
