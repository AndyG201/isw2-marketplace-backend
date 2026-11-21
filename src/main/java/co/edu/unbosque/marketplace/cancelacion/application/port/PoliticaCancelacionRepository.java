package co.edu.unbosque.marketplace.cancelacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;

public interface PoliticaCancelacionRepository extends RepositorioBasico<PoliticaCancelacion, UUID> {
    Optional<PoliticaCancelacion> findByIdPoliticaAndActivaTrue(UUID idPolitica);
    List<PoliticaCancelacion> findByActivaTrueOrderByNombreAsc();
}
