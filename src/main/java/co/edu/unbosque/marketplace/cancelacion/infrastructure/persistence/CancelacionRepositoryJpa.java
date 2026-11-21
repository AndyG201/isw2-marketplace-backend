package co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.cancelacion.domain.Cancelacion;
import co.edu.unbosque.marketplace.cancelacion.application.port.CancelacionRepository;

@Repository
public interface CancelacionRepositoryJpa extends JpaRepository<Cancelacion, UUID>,
        CancelacionRepository {

    Optional<Cancelacion> findByIdReserva(UUID idReserva);
}
