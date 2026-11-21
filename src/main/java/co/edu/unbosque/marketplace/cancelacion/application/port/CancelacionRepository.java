package co.edu.unbosque.marketplace.cancelacion.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.cancelacion.domain.Cancelacion;

public interface CancelacionRepository extends RepositorioBasico<Cancelacion, UUID> {
    Optional<Cancelacion> findByIdReserva(UUID idReserva);
}
