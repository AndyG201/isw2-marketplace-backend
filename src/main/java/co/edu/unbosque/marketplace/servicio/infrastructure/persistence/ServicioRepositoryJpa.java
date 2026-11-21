
package co.edu.unbosque.marketplace.servicio.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.servicio.domain.Servicio;

import java.util.UUID;
import co.edu.unbosque.marketplace.servicio.application.port.ServicioRepository;

@Repository
public interface ServicioRepositoryJpa extends JpaRepository<Servicio, UUID>,
        ServicioRepository {

}
