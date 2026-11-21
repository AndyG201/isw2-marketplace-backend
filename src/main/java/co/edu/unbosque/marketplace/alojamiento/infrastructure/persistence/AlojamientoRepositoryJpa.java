
package co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.application.port.AlojamientoRepository;

@Repository
public interface AlojamientoRepositoryJpa extends JpaRepository<Alojamiento, UUID>,
        AlojamientoRepository {

    List<Alojamiento> findByIdAnfitrion(UUID idAnfitrion);
}
