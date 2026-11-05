
package co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;

@Repository
public interface AlojamientoRepository
        extends JpaRepository<Alojamiento, UUID> {

    List<Alojamiento> findByIdAnfitrion(UUID idAnfitrion);
}
