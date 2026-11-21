package co.edu.unbosque.marketplace.ubicacion.application.port;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.marketplace.ubicacion.domain.Ciudad;

public interface CiudadRepository extends JpaRepository<Ciudad, UUID> {
    List<Ciudad> findAllByPaisIdPaisOrderByNombreAsc(UUID idPais);
    boolean existsByIdCiudad(UUID idCiudad);
    boolean existsByPaisIdPais(UUID idPais);
}
