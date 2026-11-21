package co.edu.unbosque.marketplace.resena.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.resena.domain.Resena;

public interface ResenaRepository extends RepositorioBasico<Resena, UUID> {
    Optional<Resena> findByIdReserva(UUID idReserva);
}
