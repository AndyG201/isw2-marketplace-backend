package co.edu.unbosque.marketplace.reserva.application.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;

public interface ReservaRepository {

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
    <S extends Reserva> List<S> saveAllAndFlush(Iterable<S> entidades);

    List<Reserva> findByIdCotizacionIn(Collection<UUID> idsCotizaciones);
    List<Reserva> buscarConfirmadasFinalizadas(LocalDate hoy);
}
