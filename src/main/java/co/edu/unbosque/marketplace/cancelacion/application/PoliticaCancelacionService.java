package co.edu.unbosque.marketplace.cancelacion.application;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.cancelacion.api.response.PoliticaCancelacionResponse;
import co.edu.unbosque.marketplace.cancelacion.domain.TipoPoliticaCancelacion;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;

@Service
public class PoliticaCancelacionService {

    private final PoliticaCancelacionRepository politicaRepository;

    public PoliticaCancelacionService(
            PoliticaCancelacionRepository politicaRepository) {

        this.politicaRepository = politicaRepository;
    }

    public List<PoliticaCancelacionResponse> consultarPoliticasActivas() {

        return politicaRepository
                .findByActivaTrueOrderByNombreAsc()
                .stream()
                .filter(politica ->
                        TipoPoliticaCancelacion.esPermitida(
                                politica.getNombre()
                        )
                )
                .map(politica -> new PoliticaCancelacionResponse(
                        politica.getIdPolitica(),
                        politica.getNombre(),
                        politica.getVersion(),
                        politica.getDescripcion(),
                        politica.getAnticipacionHoras(),
                        politica.getPorcentajeDevolucion()
                ))
                .toList();
    }
}
