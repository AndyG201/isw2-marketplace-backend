package co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;
import co.edu.unbosque.marketplace.cancelacion.application.port.PoliticaCancelacionRepository;

@Repository
public interface PoliticaCancelacionRepositoryJpa extends JpaRepository<PoliticaCancelacion, UUID>,
        PoliticaCancelacionRepository {

    Optional<PoliticaCancelacion> findByIdPoliticaAndActivaTrue(
            UUID idPolitica
    );
    List<PoliticaCancelacion> findByActivaTrueOrderByNombreAsc();
}
