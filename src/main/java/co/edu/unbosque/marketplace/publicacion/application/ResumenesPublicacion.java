package co.edu.unbosque.marketplace.publicacion.application;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;

final class ResumenesPublicacion {

    private static final Comparator<ImagenPublicacion> PRIORIDAD =
            Comparator.comparing((ImagenPublicacion imagen) -> !imagen.isEsPortada())
                    .thenComparingInt(ImagenPublicacion::getOrden);

    private ResumenesPublicacion() {
    }

    static Map<UUID, String> portadas(
            ImagenPublicacionRepository repositorio,
            List<Publicacion> publicaciones) {

        return consultar(
                repositorio,
                publicaciones.stream().map(Publicacion::getIdPublicacion).toList());
    }

    static PublicacionResumenResponse resumen(
            Publicacion publicacion,
            AlojamientoPublicacionInfo alojamiento,
            Map<UUID, String> portadas) {

        return new PublicacionResumenResponse(
                publicacion.getIdPublicacion(),
                alojamiento.idAlojamiento(),
                publicacion.getTitulo(),
                alojamiento.ciudad(),
                alojamiento.tipoAlojamiento(),
                alojamiento.capacidad(),
                publicacion.getPrecioPorNoche(),
                publicacion.getEstado(),
                publicacion.getFechaCreacion(),
                portadas.get(publicacion.getIdPublicacion()));
    }

    static Map<UUID, String> consultar(
            ImagenPublicacionRepository repositorio,
            Collection<UUID> idsPublicaciones) {

        if (idsPublicaciones.isEmpty()) {
            return Map.of();
        }

        List<ImagenPublicacion> imagenes =
                repositorio.findByIdPublicacionInOrderByOrdenAsc(idsPublicaciones);

        return imagenes.stream()
                .collect(Collectors.groupingBy(
                        ImagenPublicacion::getIdPublicacion,
                        Collectors.collectingAndThen(
                                Collectors.minBy(PRIORIDAD),
                                imagen -> imagen
                                        .map(ImagenPublicacion::getRutaImagen)
                                        .orElse(null)
                        )
                ));
    }
}
