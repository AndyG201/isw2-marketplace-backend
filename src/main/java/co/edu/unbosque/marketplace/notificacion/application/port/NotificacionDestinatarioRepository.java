package co.edu.unbosque.marketplace.notificacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificacionDestinatarioRepository {

    Optional<DestinatariosReserva> buscarDestinatariosReserva(UUID idReserva);

    Optional<UUID> buscarAnfitrionPublicacion(UUID idPublicacion);

    List<UUID> buscarAdministradoresActivos();

    record DestinatariosReserva(UUID idHuesped, UUID idAnfitrion, UUID idPublicacion) {
    }
}
