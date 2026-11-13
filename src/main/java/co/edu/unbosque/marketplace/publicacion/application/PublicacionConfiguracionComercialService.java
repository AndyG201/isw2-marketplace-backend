package co.edu.unbosque.marketplace.publicacion.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.request.ConfigurarConfiguracionComercialRequest;
import co.edu.unbosque.marketplace.publicacion.api.request.ServicioAdicionalRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.ConfiguracionComercialPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.ServicioAdicionalPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalCommand;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalData;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class PublicacionConfiguracionComercialService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final UsuarioPublicacionQuery usuarioQuery;
    private final ServicioAdicionalCommand servicioAdicionalCommand;
    private final ServicioAdicionalQuery servicioAdicionalQuery;

    public PublicacionConfiguracionComercialService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            UsuarioPublicacionQuery usuarioQuery,
            ServicioAdicionalCommand servicioAdicionalCommand,
            ServicioAdicionalQuery servicioAdicionalQuery) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.usuarioQuery = usuarioQuery;
        this.servicioAdicionalCommand = servicioAdicionalCommand;
        this.servicioAdicionalQuery = servicioAdicionalQuery;
    }

    @Transactional
    public ConfiguracionComercialPublicacionResponse actualizarConfiguracion(
            UUID idAnfitrion,
            UUID idPublicacion,
            ConfigurarConfiguracionComercialRequest solicitud) {

        validarIdentificadores(idAnfitrion, idPublicacion);

        validarSolicitud(solicitud);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarPropietario(idAnfitrion, publicacion);

        publicacion.setTarifaLimpieza(solicitud.getTarifaLimpieza());
        publicacion.setFechaActualizacion(OffsetDateTime.now());

        publicacionRepository.save(publicacion);

        List<ServicioAdicionalData> nuevosServicios =
                solicitud.getServiciosAdicionales()
                        .stream()
                        .map(this::convertirSolicitud)
                        .toList();

        List<ServicioAdicionalInfo> serviciosGuardados =
                servicioAdicionalCommand.reemplazarActivosPorPublicacion(
                        idPublicacion,
                        nuevosServicios
                );

        return construirRespuesta(publicacion, serviciosGuardados);
    }

    @Transactional(readOnly = true)
    public ConfiguracionComercialPublicacionResponse consultarConfiguracion(
            UUID idAnfitrion,
            UUID idPublicacion) {

        validarIdentificadores(idAnfitrion, idPublicacion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarPropietario(idAnfitrion, publicacion);

        List<ServicioAdicionalInfo> servicios =
                servicioAdicionalQuery.consultarActivosPorPublicacion(
                        idPublicacion
                );

        return construirRespuesta(publicacion, servicios);
    }

    private Publicacion obtenerPublicacion(UUID idPublicacion) {

        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La publicación no existe."
                        ));
    }

    private void validarPropietario(
            UUID idAnfitrion,
            Publicacion publicacion) {

        UsuarioPublicacionInfo usuario = usuarioQuery
                .consultarParaPublicacion(idAnfitrion)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "No tienes permisos para configurar esta publicación."
                        ));

        if (!usuario.esAnfitrion()) {
            throw new AccessDeniedException(
                    "Solo el anfitrión propietario puede configurar esta publicación."
            );
        }

        AlojamientoPublicacionInfo alojamiento = alojamientoQuery
                .consultarDatosParaPublicacion(
                        publicacion.getIdAlojamiento()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado a la publicación."
                        ));

        if (!idAnfitrion.equals(alojamiento.idAnfitrion())) {
            throw new AccessDeniedException(
                    "No tienes permisos para modificar la configuración comercial de esta publicación."
            );
        }
    }

    private void validarIdentificadores(
            UUID idAnfitrion,
            UUID idPublicacion) {

        if (idAnfitrion == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para configurar la publicación."
            );
        }

        if (idPublicacion == null) {
            throw new IllegalArgumentException(
                    "El identificador de la publicación es obligatorio."
            );
        }
    }

    private void validarSolicitud(
            ConfigurarConfiguracionComercialRequest solicitud) {

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La configuración comercial es obligatoria."
            );
        }

        BigDecimal tarifa = solicitud.getTarifaLimpieza();

        if (tarifa == null || tarifa.signum() < 0) {
            throw new IllegalArgumentException(
                    "La tarifa de limpieza no puede ser negativa ni nula."
            );
        }

        if (solicitud.getServiciosAdicionales() == null) {
            throw new IllegalArgumentException(
                    "Debes proporcionar la lista de servicios adicionales."
            );
        }

        Set<String> nombres = new HashSet<>();

        for (ServicioAdicionalRequest servicio :
                solicitud.getServiciosAdicionales()) {

            if (servicio == null
                    || servicio.getNombre() == null
                    || servicio.getNombre().isBlank()
                    || servicio.getNombre().trim().length() > 100) {

                throw new IllegalArgumentException(
                        "Cada servicio debe tener un nombre válido de máximo 100 caracteres."
                );
            }

            if (servicio.getCosto() == null
                    || servicio.getCosto().signum() < 0) {

                throw new IllegalArgumentException(
                        "El costo de los servicios adicionales no puede ser negativo."
                );
            }

            String nombreNormalizado =
                    servicio.getNombre().trim().toLowerCase(Locale.ROOT);

            if (!nombres.add(nombreNormalizado)) {
                throw new IllegalArgumentException(
                        "No puedes registrar servicios adicionales con nombres duplicados."
                );
            }
        }
    }

    private ServicioAdicionalData convertirSolicitud(
            ServicioAdicionalRequest solicitud) {

        String descripcion = solicitud.getDescripcion();

        if (descripcion != null && descripcion.isBlank()) {
            descripcion = null;
        } else if (descripcion != null) {
            descripcion = descripcion.trim();
        }

        return new ServicioAdicionalData(
                solicitud.getNombre().trim(),
                descripcion,
                solicitud.getCosto()
        );
    }

    private ConfiguracionComercialPublicacionResponse construirRespuesta(
            Publicacion publicacion,
            List<ServicioAdicionalInfo> servicios) {

        List<ServicioAdicionalPublicacionResponse> respuestas =
                servicios.stream()
                        .map(servicio ->
                                new ServicioAdicionalPublicacionResponse(
                                        servicio.idAdicional(),
                                        servicio.nombre(),
                                        servicio.descripcion(),
                                        servicio.costo(),
                                        servicio.activo()
                                )
                        )
                        .toList();

        return new ConfiguracionComercialPublicacionResponse(
                publicacion.getIdPublicacion(),
                publicacion.getTarifaLimpieza(),
                respuestas,
                publicacion.getFechaActualizacion()
        );
    }
}
