package co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.cancelacion.domain.Cancelacion;

@Repository
public interface CancelacionRepository extends JpaRepository<Cancelacion, UUID> {

    Optional<Cancelacion> findByIdReserva(UUID idReserva);
}
