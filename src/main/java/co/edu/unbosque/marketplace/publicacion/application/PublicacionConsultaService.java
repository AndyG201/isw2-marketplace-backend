
package co.edu.unbosque.marketplace.publicacion.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.publicacion.api.response.ImagenPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.MisPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.servicio.api.response.ServicioResponse;
import co.edu.unbosque.marketplace.servicio.domain.Servicio;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;

@Service
public class PublicacionConsultaService {

    private final AlojamientoRepository alojamientoRepository;
    private final PublicacionRepository publicacionRepository;
    private final ImagenPublicacionRepository imagenRepository;
    private final PublicacionServicioRepository publicacionServicioRepository;
    private final ServicioRepository servicioRepository;

    public PublicacionConsultaService(
            AlojamientoRepository alojamientoRepository,
            PublicacionRepository publicacionRepository,
            ImagenPublicacionRepository imagenRepository,
            PublicacionServicioRepository publicacionServicioRepository,
            ServicioRepository servicioRepository) {

        this.alojamientoRepository = alojamientoRepository;
        this.publicacionRepository = publicacionRepository;
        this.imagenRepository = imagenRepository;
        this.publicacionServicioRepository = publicacionServicioRepository;
        this.servicioRepository = servicioRepository;
    }

    public MisPublicacionesResponse consultarMisPublicaciones(
            UUID idAnfitrion) {

        if (idAnfitrion == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al anfitrión.");
        }

        List<Alojamiento> alojamientos =
                alojamientoRepository.findByIdAnfitrion(idAnfitrion);

        if (alojamientos.isEmpty()) {
            return new MisPublicacionesResponse(
                    "No tienes publicaciones registradas.",
                    List.of()
            );
        }

        List<UUID> idsAlojamientos = alojamientos.stream()
                .map(Alojamiento::getIdAlojamiento)
                .toList();

        Map<UUID, Alojamiento> alojamientosPorId = alojamientos.stream()
                .collect(Collectors.toMap(
                        Alojamiento::getIdAlojamiento,
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

                            Alojamiento alojamiento =
                                    alojamientosPorId.get(
                                            publicacion.getIdAlojamiento()
                                    );

                            return new PublicacionResumenResponse(
                                    publicacion.getIdPublicacion(),
                                    alojamiento.getIdAlojamiento(),
                                    publicacion.getTitulo(),
                                    alojamiento.getCiudad(),
                                    alojamiento.getTipoAlojamiento(),
                                    alojamiento.getCapacidad(),
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

        List<Alojamiento> alojamientos =
                alojamientoRepository.findByIdAnfitrion(idAnfitrion);

        List<UUID> idsAlojamientos = alojamientos.stream()
                .map(Alojamiento::getIdAlojamiento)
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

        Alojamiento alojamiento = alojamientos.stream()
                .filter(a -> a.getIdAlojamiento()
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

        List<ServicioResponse> servicios =
                servicioRepository.findAllById(idsServicios)
                        .stream()
                        .map(servicio -> new ServicioResponse(
                                servicio.getIdServicio(),
                                servicio.getNombre(),
                                servicio.getDescripcion()
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
                alojamiento.getUbicacion(),
                alojamiento.getCiudad(),
                alojamiento.getTipoAlojamiento(),
                alojamiento.getCapacidad(),
                servicios,
                imagenes
        );
    }
}
