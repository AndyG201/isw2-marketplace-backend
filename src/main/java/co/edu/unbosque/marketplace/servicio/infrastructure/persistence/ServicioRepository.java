
package co.edu.unbosque.marketplace.servicio.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.servicio.domain.Servicio;

import java.util.UUID;

@Repository
public interface ServicioRepository
        extends JpaRepository<Servicio, UUID> {

}
