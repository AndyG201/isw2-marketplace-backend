package co.edu.unbosque.marketplace.cotizacion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;
import co.edu.unbosque.marketplace.cotizacion.application.port.CotizacionRepository;

@Repository
public interface CotizacionRepositoryJpa extends JpaRepository<Cotizacion, UUID>,
        CotizacionRepository {

    List<Cotizacion> findByIdPublicacion(UUID idPublicacion);
}
