package co.edu.unbosque.marketplace.ubicacion.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.marketplace.ubicacion.domain.Pais;
import co.edu.unbosque.marketplace.ubicacion.application.port.PaisRepository;

public interface PaisRepositoryJpa extends JpaRepository<Pais, UUID>,
        PaisRepository {
    List<Pais> findAllByOrderByNombreAsc();
    Optional<Pais> findByCodigoIso2IgnoreCase(String codigoIso2);
}
