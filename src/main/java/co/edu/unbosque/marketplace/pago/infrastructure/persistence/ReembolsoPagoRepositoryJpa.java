package co.edu.unbosque.marketplace.pago.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.pago.domain.ReembolsoPago;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsoPagoRepository;

@Repository
public interface ReembolsoPagoRepositoryJpa extends JpaRepository<ReembolsoPago, UUID>,
        ReembolsoPagoRepository {
    Optional<ReembolsoPago> findByIdPago(UUID idPago);
}
