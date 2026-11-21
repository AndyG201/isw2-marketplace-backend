package co.edu.unbosque.marketplace.reserva.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;

public interface ReservaSolicitudRepository {

    <S extends Reserva> S save(S entidad);
    <S extends Reserva> List<S> saveAll(Iterable<S> entidades);
    <S extends Reserva> S saveAndFlush(S entidad);
    Optional<Reserva> findById(UUID id);
    List<Reserva> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Reserva entidad);
    void deleteById(UUID id);
    void flush();
    Optional<Reserva> findByClaveIdempotencia(String claveIdempotencia);
    Optional<Reserva> findByIdCotizacion(UUID idCotizacion);
    Optional<ReservaSolicitudDatos> consultarDatosCotizacion(UUID idCotizacion);
}
