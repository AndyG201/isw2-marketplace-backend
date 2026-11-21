package co.edu.unbosque.marketplace.publicacion.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;

import co.edu.unbosque.marketplace.cotizacion.contracts.PublicacionCotizacionInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.PublicacionCotizacionQuery;

@Service
public class PublicacionCotizacionQueryService
        implements PublicacionCotizacionQuery {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;

    public PublicacionCotizacionQueryService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery) {
        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PublicacionCotizacionInfo> consultarPublicacionActiva(
            UUID idPublicacion) {

        if (idPublicacion == null) {
            return Optional.empty();
        }

        return publicacionRepository.findById(idPublicacion)
                .filter(publicacion ->
                        ESTADO_ACTIVA.equals(publicacion.getEstado()))
                .flatMap(this::convertir);
    }

    private Optional<PublicacionCotizacionInfo> convertir(
            Publicacion publicacion) {

        return alojamientoQuery
                .consultarDatosParaPublicacion(
                        publicacion.getIdAlojamiento()
                )
                .map(alojamiento -> new PublicacionCotizacionInfo(
                        publicacion.getIdPublicacion(),
                        publicacion.getIdAlojamiento(),
                        publicacion.getPrecioPorNoche(),
                        publicacion.getTarifaLimpieza(),
                        alojamiento.capacidad()
                ));
    }
}