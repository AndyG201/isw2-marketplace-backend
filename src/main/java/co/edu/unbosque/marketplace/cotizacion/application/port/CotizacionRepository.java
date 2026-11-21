package co.edu.unbosque.marketplace.cotizacion.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;

public interface CotizacionRepository extends RepositorioBasico<Cotizacion, UUID> {
    List<Cotizacion> findByIdPublicacion(UUID idPublicacion);
}
