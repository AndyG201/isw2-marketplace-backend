package co.edu.unbosque.marketplace.resena.application.port;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.resena.domain.Resena;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, UUID> {

    Optional<Resena> findByIdReserva(UUID idReserva);
}
