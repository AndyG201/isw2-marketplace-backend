package co.edu.unbosque.marketplace.ubicacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import co.edu.unbosque.marketplace.ubicacion.domain.Pais;

public interface PaisRepository extends JpaRepository<Pais, UUID> {
    List<Pais> findAllByOrderByNombreAsc();
    Optional<Pais> findByCodigoIso2IgnoreCase(String codigoIso2);
}
