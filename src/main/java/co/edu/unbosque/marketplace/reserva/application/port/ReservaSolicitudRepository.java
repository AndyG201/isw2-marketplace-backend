package co.edu.unbosque.marketplace.reserva.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

public interface ReservaSolicitudRepository extends RepositorioBasico<Reserva, UUID> {
    Optional<Reserva> findByClaveIdempotencia(String claveIdempotencia);
    Optional<Reserva> findByIdCotizacion(UUID idCotizacion);
    Optional<ReservaSolicitudDatos> consultarDatosCotizacion(UUID idCotizacion);
}
