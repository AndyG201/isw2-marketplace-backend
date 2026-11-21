package co.edu.unbosque.marketplace.pago.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.pago.domain.ReembolsoPago;

public interface ReembolsoPagoRepository extends RepositorioBasico<ReembolsoPago, UUID> {
    Optional<ReembolsoPago> findByIdPago(UUID idPago);
}
