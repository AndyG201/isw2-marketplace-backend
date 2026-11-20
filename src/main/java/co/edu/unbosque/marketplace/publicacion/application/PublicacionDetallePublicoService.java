package co.edu.unbosque.marketplace.publicacion.application;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.PoliticaCancelacionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionPublicaResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;

@Service
public class PublicacionDetallePublicoService {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final PublicacionConsultaService consultaService;
    private final PublicacionServiciosAdicionalesSeleccionService adicionalesService;
    private final PoliticaCancelacionQuery politicaQuery;

    public PublicacionDetallePublicoService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            PublicacionConsultaService consultaService,
            PublicacionServiciosAdicionalesSeleccionService adicionalesService,
            PoliticaCancelacionQuery politicaQuery) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.consultaService = consultaService;
        this.adicionalesService = adicionalesService;
        this.politicaQuery = politicaQuery;
    }

    @Transactional(readOnly = true)
    public PublicacionPublicaResponse consultar(UUID idPublicacion) {

        if (idPublicacion == null) {
            throw new NoSuchElementException("La publicación no existe o no está disponible.");
        }

        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .filter(encontrada -> ESTADO_ACTIVA.equals(encontrada.getEstado()))
                .orElseThrow(() -> new NoSuchElementException(
                        "La publicación no existe o no está disponible."));

        AlojamientoPublicacionInfo alojamiento = alojamientoQuery
                .consultarDatosParaPublicacion(publicacion.getIdAlojamiento())
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el alojamiento asociado."));

        PoliticaCancelacionPublicacionResponse politica = politicaQuery
                .consultarPorId(publicacion.getIdPolitica())
                .map(info -> convertirPolitica(idPublicacion, info))
                .orElse(null);

        return new PublicacionPublicaResponse(
                consultaService.armarDetalle(publicacion, alojamiento),
                adicionalesService.consultarDisponibles(idPublicacion),
                politica);
    }

    private static PoliticaCancelacionPublicacionResponse convertirPolitica(
            UUID idPublicacion,
            PoliticaCancelacionInfo info) {

        return new PoliticaCancelacionPublicacionResponse(
                idPublicacion,
                info.idPolitica(),
                info.nombre(),
                info.version(),
                info.descripcion(),
                info.anticipacionHoras(),
                info.porcentajeDevolucion(),
                info.activa());
    }
}
