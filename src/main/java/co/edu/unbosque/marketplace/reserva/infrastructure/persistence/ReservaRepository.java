package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@Repository
public interface ReservaRepository
        extends JpaRepository<Reserva, UUID> {

    List<Reserva> findByIdCotizacionIn(
            Collection<UUID> idsCotizaciones
    );
}
