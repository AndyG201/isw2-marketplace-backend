package co.edu.unbosque.marketplace.publicacion.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.publicacion.application.dto.SeleccionServiciosAdicionalesResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.ServicioAdicionalPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;

@Service
public class PublicacionServiciosAdicionalesSeleccionService {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final PublicacionRepository publicacionRepository;
    private final ServicioAdicionalQuery servicioAdicionalQuery;

    public PublicacionServiciosAdicionalesSeleccionService(
            PublicacionRepository publicacionRepository,
            ServicioAdicionalQuery servicioAdicionalQuery) {

        this.publicacionRepository = publicacionRepository;
        this.servicioAdicionalQuery = servicioAdicionalQuery;
    }

    @Transactional(readOnly = true)
    public List<ServicioAdicionalPublicacionResponse> consultarDisponibles(
            UUID idPublicacion) {

        validarPublicacionVisible(idPublicacion);

        return consultarRegistrados(idPublicacion);
    }

    @Transactional(readOnly = true)
    public List<ServicioAdicionalPublicacionResponse> consultarRegistrados(
            UUID idPublicacion) {

        return servicioAdicionalQuery
                .consultarActivosPorPublicacion(idPublicacion)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeleccionServiciosAdicionalesResponse validarSeleccion(
            UUID idPublicacion,
            List<UUID> idsAdicionales) {

        validarPublicacionVisible(idPublicacion);

        if (idsAdicionales == null) {
            throw new IllegalArgumentException(
                    "Debes enviar la lista de servicios seleccionados."
            );
        }

        List<ServicioAdicionalInfo> disponibles =
                servicioAdicionalQuery.consultarActivosPorPublicacion(
                        idPublicacion
                );

        Map<UUID, ServicioAdicionalInfo> disponiblesPorId =
                disponibles.stream().collect(Collectors.toMap(
                        ServicioAdicionalInfo::idAdicional,
                        Function.identity()
                ));

        Set<UUID> identificadoresUnicos = new HashSet<>();
        List<ServicioAdicionalPublicacionResponse> seleccionados =
                new ArrayList<>();
        BigDecimal costoTotal = BigDecimal.ZERO;

        for (UUID idAdicional : idsAdicionales) {
            if (idAdicional == null) {
                throw new IllegalArgumentException(
                        "El identificador de cada servicio es obligatorio."
                );
            }

            if (!identificadoresUnicos.add(idAdicional)) {
                throw new IllegalArgumentException(
                        "No puedes seleccionar el mismo servicio más de una vez."
                );
            }

            ServicioAdicionalInfo servicio =
                    disponiblesPorId.get(idAdicional);

            if (servicio == null) {
                throw new IllegalArgumentException(
                        "Uno de los servicios no está disponible para esta publicación."
                );
            }

            seleccionados.add(convertirRespuesta(servicio));
            costoTotal = costoTotal.add(servicio.costo());
        }

        return new SeleccionServiciosAdicionalesResponse(
                idPublicacion,
                seleccionados,
                costoTotal
        );
    }

    private void validarPublicacionVisible(UUID idPublicacion) {
        if (idPublicacion == null) {
            throw new IllegalArgumentException(
                    "El identificador de la publicación es obligatorio."
            );
        }

        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new NoSuchElementException(
                        "La publicación no existe."
                ));

        if (!ESTADO_ACTIVA.equals(publicacion.getEstado())) {
            throw new NoSuchElementException(
                    "La publicación no está disponible en el marketplace."
            );
        }
    }

    private ServicioAdicionalPublicacionResponse convertirRespuesta(
            ServicioAdicionalInfo servicio) {

        return new ServicioAdicionalPublicacionResponse(
                servicio.idAdicional(),
                servicio.nombre(),
                servicio.descripcion(),
                servicio.costo(),
                servicio.activo()
        );
    }
}