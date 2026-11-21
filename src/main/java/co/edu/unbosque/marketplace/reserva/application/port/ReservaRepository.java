package co.edu.unbosque.marketplace.reserva.application.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

public interface ReservaRepository extends RepositorioBasico<Reserva, UUID> {
    <S extends Reserva> List<S> saveAllAndFlush(Iterable<S> entidades);

    List<Reserva> findByIdCotizacionIn(Collection<UUID> idsCotizaciones);
    List<Reserva> buscarConfirmadasFinalizadas(LocalDate hoy);
}
