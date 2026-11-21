package co.edu.unbosque.marketplace.pago.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.pago.domain.Pago;
import co.edu.unbosque.marketplace.pago.application.port.PagoRepository;

@Repository
public interface PagoRepositoryJpa extends JpaRepository<Pago, UUID>,
        PagoRepository {
    Optional<Pago> findByClaveIdempotencia(String claveIdempotencia);
    Optional<Pago> findFirstByIdReservaOrderByFechaCreacionDesc(UUID idReserva);
}
