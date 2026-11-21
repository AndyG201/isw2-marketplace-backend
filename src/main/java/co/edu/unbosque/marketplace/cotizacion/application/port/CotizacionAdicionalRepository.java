package co.edu.unbosque.marketplace.cotizacion.application.port;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.cotizacion.domain.CotizacionAdicional;
import co.edu.unbosque.marketplace.cotizacion.domain.CotizacionAdicionalId;

@Repository
public interface CotizacionAdicionalRepository
        extends JpaRepository<CotizacionAdicional, CotizacionAdicionalId> {
}