package co.edu.unbosque.marketplace.ubicacion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.marketplace.ubicacion.domain.Ciudad;
import co.edu.unbosque.marketplace.ubicacion.application.port.CiudadRepository;

public interface CiudadRepositoryJpa extends JpaRepository<Ciudad, UUID>,
        CiudadRepository {
    List<Ciudad> findAllByPaisIdPaisOrderByNombreAsc(UUID idPais);
    boolean existsByIdCiudad(UUID idCiudad);
    boolean existsByPaisIdPais(UUID idPais);
}
