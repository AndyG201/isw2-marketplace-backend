package co.edu.unbosque.marketplace.cotizacion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;

@Repository
public interface CotizacionRepository
        extends JpaRepository<Cotizacion, UUID> {

    List<Cotizacion> findByIdPublicacion(UUID idPublicacion);
}
