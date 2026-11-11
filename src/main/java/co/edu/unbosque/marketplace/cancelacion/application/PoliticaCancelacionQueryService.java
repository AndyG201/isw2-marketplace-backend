
package co.edu.unbosque.marketplace.cancelacion.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;

@Service
public class PoliticaCancelacionQueryService
        implements PoliticaCancelacionQuery {

    private final PoliticaCancelacionRepository politicaRepository;

    public PoliticaCancelacionQueryService(
            PoliticaCancelacionRepository politicaRepository) {

        this.politicaRepository = politicaRepository;
    }

    @Override
    public boolean existePoliticaActiva(UUID idPolitica) {

        if (idPolitica == null) {
            return false;
        }

        return politicaRepository
                .findByIdPoliticaAndActivaTrue(idPolitica)
                .isPresent();
    }
}
