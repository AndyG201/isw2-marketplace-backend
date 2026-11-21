package co.edu.unbosque.marketplace.publicacion.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.application.dto.PoliticaCancelacionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionAdministracionResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionPublicaResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.ServicioAdicionalPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@Service
public class PublicacionDetallePublicoService {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final PublicacionConsultaService consultaService;
    private final PublicacionServiciosAdicionalesSeleccionService adicionalesService;
    private final PoliticaCancelacionQuery politicaQuery;
    private final CambiosPublicacionService cambios;

    public PublicacionDetallePublicoService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            PublicacionConsultaService consultaService,
            PublicacionServiciosAdicionalesSeleccionService adicionalesService,
            PoliticaCancelacionQuery politicaQuery,
            CambiosPublicacionService cambios) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.consultaService = consultaService;
        this.adicionalesService = adicionalesService;
        this.politicaQuery = politicaQuery;
        this.cambios = cambios;
    }

    @Transactional(readOnly = true)
    public PublicacionPublicaResponse consultar(UUID idPublicacion) {

        Publicacion publicacion = buscar(idPublicacion)
                .filter(encontrada -> ESTADO_ACTIVA.equals(encontrada.getEstado()))
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La publicación no existe o no está disponible."));

        return armar(idPublicacion, publicacion, adicionalesService.consultarDisponibles(idPublicacion));
    }

    @Transactional(readOnly = true)
    public PublicacionAdministracionResponse consultarParaAdministracion(UUID idPublicacion) {

        Publicacion publicacion = buscar(idPublicacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("La publicación no existe."));

        PublicacionPublicaResponse detalle =
                armar(idPublicacion, publicacion, adicionalesService.consultarRegistrados(idPublicacion), true);

        return new PublicacionAdministracionResponse(
                detalle.publicacion(),
                detalle.serviciosAdicionales(),
                detalle.politica(),
                cambios.pendiente(idPublicacion).orElse(null));
    }

    private Optional<Publicacion> buscar(UUID idPublicacion) {

        return idPublicacion == null
                ? Optional.empty()
                : publicacionRepository.findById(idPublicacion);
    }

    private PublicacionPublicaResponse armar(
            UUID idPublicacion,
            Publicacion publicacion,
            List<ServicioAdicionalPublicacionResponse> adicionales) {

        return armar(idPublicacion, publicacion, adicionales, false);
    }

    private PublicacionPublicaResponse armar(
            UUID idPublicacion,
            Publicacion publicacion,
            List<ServicioAdicionalPublicacionResponse> adicionales,
            boolean incluirPropietario) {

        AlojamientoPublicacionInfo alojamiento = alojamientoQuery
                .consultarDatosParaPublicacion(publicacion.getIdAlojamiento())
                .orElseThrow(() -> new ConflictoNegocioException(
                        "No se encontró el alojamiento asociado."));

        PoliticaCancelacionPublicacionResponse politica = politicaQuery
                .consultarPorId(publicacion.getIdPolitica())
                .map(info -> convertirPolitica(idPublicacion, info))
                .orElse(null);

        PublicacionDetalleResponse detalle = consultaService.armarDetalle(publicacion, alojamiento);
        if (incluirPropietario && detalle != null) {
            detalle.conPropietario(alojamiento.propietarioInmueble());
        }

        return new PublicacionPublicaResponse(detalle, adicionales, politica);
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
