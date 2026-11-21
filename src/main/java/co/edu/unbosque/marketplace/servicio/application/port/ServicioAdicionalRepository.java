package co.edu.unbosque.marketplace.servicio.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.servicio.domain.ServicioAdicional;

public interface ServicioAdicionalRepository extends RepositorioBasico<ServicioAdicional, UUID> {
    List<ServicioAdicional> findByIdPublicacionAndActivoTrueOrderByNombreAsc(UUID idPublicacion);
}
