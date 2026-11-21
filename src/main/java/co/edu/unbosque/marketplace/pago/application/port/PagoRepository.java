package co.edu.unbosque.marketplace.pago.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.pago.domain.Pago;

public interface PagoRepository extends RepositorioBasico<Pago, UUID> {
    Optional<Pago> findByClaveIdempotencia(String claveIdempotencia);
    Optional<Pago> findFirstByIdReservaOrderByFechaCreacionDesc(UUID idReserva);
}
