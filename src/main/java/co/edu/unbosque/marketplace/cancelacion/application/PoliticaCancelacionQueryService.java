
package co.edu.unbosque.marketplace.cancelacion.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;
import co.edu.unbosque.marketplace.cancelacion.domain.TipoPoliticaCancelacion;
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

        return consultarPoliticaSeleccionable(idPolitica)
                .isPresent();
    }

    @Override
    public Optional<PoliticaCancelacionInfo> consultarPoliticaSeleccionable(
            UUID idPolitica) {

        if (idPolitica == null) {
            return Optional.empty();
        }

        return politicaRepository
                .findByIdPoliticaAndActivaTrue(idPolitica)
                .filter(politica ->
                        TipoPoliticaCancelacion.esPermitida(
                                politica.getNombre()
                        )
                )
                .map(this::convertir);
    }

    @Override
    public Optional<PoliticaCancelacionInfo> consultarPorId(
            UUID idPolitica) {

        if (idPolitica == null) {
            return Optional.empty();
        }

        return politicaRepository.findById(idPolitica)
                .map(this::convertir);
    }

    private PoliticaCancelacionInfo convertir(
            PoliticaCancelacion politica) {

        return new PoliticaCancelacionInfo(
                politica.getIdPolitica(),
                politica.getNombre(),
                politica.getVersion(),
                politica.getDescripcion(),
                politica.getAnticipacionHoras(),
                politica.getPorcentajeDevolucion(),
                politica.isActiva()
        );
    }
}
