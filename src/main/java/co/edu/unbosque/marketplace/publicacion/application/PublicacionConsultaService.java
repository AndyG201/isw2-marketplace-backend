
package co.edu.unbosque.marketplace.publicacion.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.ImagenPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.MisPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.ServicioPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;

@Service
public class PublicacionConsultaService {

    private final AlojamientoPublicacionQuery alojamientoPublicacionQuery;
    private final PublicacionRepository publicacionRepository;
    private final ImagenPublicacionRepository imagenRepository;
    private final PublicacionServicioRepository publicacionServicioRepository;
    private final ServicioPublicacionQuery servicioPublicacionQuery;

    public PublicacionConsultaService(
            AlojamientoPublicacionQuery alojamientoPublicacionQuery,
            PublicacionRepository publicacionRepository,
            ImagenPublicacionRepository imagenRepository,
            PublicacionServicioRepository publicacionServicioRepository,
            ServicioPublicacionQuery servicioPublicacionQuery) {

        this.alojamientoPublicacionQuery = alojamientoPublicacionQuery;
        this.publicacionRepository = publicacionRepository;
        this.imagenRepository = imagenRepository;
        this.publicacionServicioRepository = publicacionServicioRepository;
        this.servicioPublicacionQuery = servicioPublicacionQuery;
    }

    public MisPublicacionesResponse consultarMisPublicaciones(
            UUID idAnfitrion) {

        if (idAnfitrion == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al anfitrión.");
        }

        List<AlojamientoPublicacionInfo> alojamientos =
                alojamientoPublicacionQuery.consultarPorAnfitrion(
                        idAnfitrion);

        if (alojamientos.isEmpty()) {
            return new MisPublicacionesResponse(
                    "No tienes publicaciones registradas.",
                    List.of()
            );
        }

        List<UUID> idsAlojamientos = alojamientos.stream()
                .map(AlojamientoPublicacionInfo::idAlojamiento)
                .toList();

        Map<UUID, AlojamientoPublicacionInfo> alojamientosPorId =
                alojamientos.stream()
                        .collect(Collectors.toMap(
                                AlojamientoPublicacionInfo::idAlojamiento,
                                Function.identity()
                        ));

        List<Publicacion> publicaciones =
                publicacionRepository
                        .findByIdAlojamientoInOrderByFechaCreacionDesc(
                                idsAlojamientos
                        );

        if (publicaciones.isEmpty()) {
            return new MisPublicacionesResponse(
                    "No tienes publicaciones registradas.",
                    List.of()
            );
        }

        List<PublicacionResumenResponse> resultado =
                publicaciones.stream()
                        .map(publicacion -> {

                            AlojamientoPublicacionInfo alojamiento =
                                    alojamientosPorId.get(
                                            publicacion.getIdAlojamiento()
                                    );

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

        return new MisPublicacionesResponse(
                "Publicaciones consultadas correctamente.",
                resultado
        );
    }

    public PublicacionDetalleResponse consultarDetalle(
            UUID idAnfitrion,
            UUID idPublicacion) {

        if (idAnfitrion == null || idPublicacion == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar la publicación.");
        }

        List<AlojamientoPublicacionInfo> alojamientos =
                alojamientoPublicacionQuery.consultarPorAnfitrion(
                        idAnfitrion);

        List<UUID> idsAlojamientos = alojamientos.stream()
                .map(AlojamientoPublicacionInfo::idAlojamiento)
                .toList();

        if (idsAlojamientos.isEmpty()) {
            throw new IllegalArgumentException(
                    "La publicación no existe o no pertenece a tu cuenta.");
        }

        Publicacion publicacion = publicacionRepository
                .findByIdPublicacionAndIdAlojamientoIn(
                        idPublicacion,
                        idsAlojamientos
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La publicación no existe o no pertenece a tu cuenta."
                        ));

        AlojamientoPublicacionInfo alojamiento = alojamientos.stream()
                .filter(a -> a.idAlojamiento()
                        .equals(publicacion.getIdAlojamiento()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado."));

        List<ImagenPublicacionResponse> imagenes =
                imagenRepository
                        .findByIdPublicacionOrderByOrdenAsc(idPublicacion)
                        .stream()
                        .map(imagen -> new ImagenPublicacionResponse(
                                imagen.getIdImagen(),
                                imagen.getRutaImagen(),
                                imagen.getOrden(),
                                imagen.isEsPortada()
                        ))
                        .toList();

        List<PublicacionServicio> relaciones =
                publicacionServicioRepository
                        .findByIdPublicacion(idPublicacion);

        List<UUID> idsServicios = relaciones.stream()
                .map(PublicacionServicio::getIdServicio)
                .toList();

        List<ServicioInfo> serviciosConsultados =
                servicioPublicacionQuery.consultarPorIds(idsServicios);

        List<ServicioPublicacionResponse> servicios =
                serviciosConsultados.stream()
                        .map(servicio -> new ServicioPublicacionResponse(
                                servicio.idServicio(),
                                servicio.nombre(),
                                servicio.descripcion()
                        ))
                        .toList();

        return new PublicacionDetalleResponse(
                publicacion.getIdPublicacion(),
                publicacion.getIdAlojamiento(),
                publicacion.getIdPolitica(),
                publicacion.getTitulo(),
                publicacion.getDescripcion(),
                publicacion.getReglas(),
                publicacion.getPrecioPorNoche(),
                publicacion.getTarifaLimpieza(),
                publicacion.getModoAceptacion(),
                publicacion.getEstado(),
                publicacion.getFechaCreacion(),
                publicacion.getFechaActualizacion(),
                alojamiento.ubicacion(),
                alojamiento.ciudad(),
                alojamiento.tipoAlojamiento(),
                alojamiento.capacidad(),
                servicios,
                imagenes
        );
    }
}
