package co.edu.unbosque.marketplace.ubicacion.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.ubicacion.domain.Ciudad;

public interface CiudadRepository extends RepositorioBasico<Ciudad, UUID> {
    List<Ciudad> findAllByPaisIdPaisOrderByNombreAsc(UUID idPais);
    boolean existsByIdCiudad(UUID idCiudad);
    boolean existsByPaisIdPais(UUID idPais);
}
