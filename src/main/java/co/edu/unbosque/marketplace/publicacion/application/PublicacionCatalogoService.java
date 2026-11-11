package co.edu.unbosque.marketplace.publicacion.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;

@Service
public class PublicacionCatalogoService {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;

    public PublicacionCatalogoService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
    }

    @Transactional(readOnly = true)
    public List<PublicacionResumenResponse> consultarCatalogo() {

        List<Publicacion> publicaciones =
                publicacionRepository
                        .findByEstadoOrderByFechaCreacionDesc(
                                ESTADO_ACTIVA
                        );

        if (publicaciones.isEmpty()) {
            return List.of();
        }

        List<UUID> idsAlojamientos = publicaciones.stream()
                .map(Publicacion::getIdAlojamiento)
                .distinct()
                .toList();

        List<AlojamientoPublicacionInfo> alojamientos =
                alojamientoQuery.consultarPorIdsAlojamiento(
                        idsAlojamientos
                );

        Map<UUID, AlojamientoPublicacionInfo> alojamientosPorId =
                alojamientos.stream()
                        .collect(Collectors.toMap(
                                AlojamientoPublicacionInfo::idAlojamiento,
                                Function.identity()
                        ));

        return publicaciones.stream()
                .map(publicacion -> {

                    AlojamientoPublicacionInfo alojamiento =
                            alojamientosPorId.get(
                                    publicacion.getIdAlojamiento()
                            );

                    if (alojamiento == null) {
                        throw new IllegalStateException(
                                "No se encontró el alojamiento asociado "
                                + "a la publicación "
                                + publicacion.getIdPublicacion()
                        );
                    }

                    return new PublicacionResumenResponse(
                            publicacion.getIdPublicacion(),
                            alojamiento.idAlojamiento(),
                            publicacion.getTitulo(),
                            alojamiento.ciudad(),
                            alojamiento.tipoAlojamiento(),
                            alojamiento.capacidad(),
                            publicacion.getPrecioPorNoche(),
                            publicacion.getEstado(),
                            publicacion.getFechaCreacion()
                    );
                })
                .toList();
    }
}
