package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.request.ConfigurarModoAceptacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.ModoAceptacionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.ModoAceptacionActualizadoEvent;
import co.edu.unbosque.marketplace.publicacion.domain.ModoAceptacionReserva;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class PublicacionModoAceptacionService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final UsuarioAdministracionQuery usuarioAdministracionQuery;
    private final UsuarioPublicacionQuery usuarioPublicacionQuery;
    private final ApplicationEventPublisher eventPublisher;

    public PublicacionModoAceptacionService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            UsuarioAdministracionQuery usuarioAdministracionQuery,
            UsuarioPublicacionQuery usuarioPublicacionQuery,
            ApplicationEventPublisher eventPublisher) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.usuarioAdministracionQuery = usuarioAdministracionQuery;
        this.usuarioPublicacionQuery = usuarioPublicacionQuery;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ModoAceptacionPublicacionResponse actualizarModoAceptacion(
            UUID idActor,
            UUID idPublicacion,
            ConfigurarModoAceptacionRequest solicitud) {

        validarIdentificadores(idActor, idPublicacion);

        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud de configuración es obligatoria."
            );
        }

        ModoAceptacionReserva modoNuevo =
                ModoAceptacionReserva.desde(
                        solicitud.getModoAceptacion()
                );

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        UUID idAnfitrion = obtenerIdAnfitrion(publicacion);

        boolean esAdministrador =
                validarPermisos(idActor, idAnfitrion);

        String modoAnterior =
                normalizarModoExistente(
                        publicacion.getModoAceptacion()
                );

        String nuevoModo = modoNuevo.name();

        // Evitar actualizaciones y notificaciones innecesarias.

        if (Objects.equals(modoAnterior, nuevoModo)) {

            return construirRespuesta(publicacion);
        }

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setModoAceptacion(nuevoModo);
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        // Solo se notifica cuando el cambio lo realiza
        // un administrador.

        if (esAdministrador) {

            eventPublisher.publishEvent(
                    new ModoAceptacionActualizadoEvent(
                            publicacion.getIdPublicacion(),
                            idAnfitrion,
                            publicacion.getTitulo(),
                            modoAnterior == null
                                    ? "SIN_CONFIGURAR"
                                    : modoAnterior,
                            nuevoModo,
                            ahora
                    )
            );
        }

        return construirRespuesta(publicacion);
    }

    @Transactional(readOnly = true)
    public ModoAceptacionPublicacionResponse consultarModoAceptacion(
            UUID idActor,
            UUID idPublicacion) {

        validarIdentificadores(idActor, idPublicacion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        UUID idAnfitrion = obtenerIdAnfitrion(publicacion);

        validarPermisos(idActor, idAnfitrion);

        if (normalizarModoExistente(
                publicacion.getModoAceptacion()) == null) {

            throw new IllegalStateException(
                    "La publicación todavía no tiene un modo de aceptación configurado."
            );
        }

        return construirRespuesta(publicacion);
    }

    private Publicacion obtenerPublicacion(UUID idPublicacion) {

        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La publicación no existe."
                        ));
    }

    private UUID obtenerIdAnfitrion(Publicacion publicacion) {

        return alojamientoQuery
                .consultarIdAnfitrion(
                        publicacion.getIdAlojamiento()
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No se encontró el anfitrión de la publicación."
                        ));
    }

    private boolean validarPermisos(
            UUID idActor,
            UUID idAnfitrion) {

        if (usuarioAdministracionQuery.esAdministrador(idActor)) {
            return true;
        }

        UsuarioPublicacionInfo usuario = usuarioPublicacionQuery
                .consultarParaPublicacion(idActor)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "No tienes permisos para modificar esta publicación."
                        ));

        if (!usuario.esAnfitrion()
                || !idActor.equals(idAnfitrion)) {

            throw new AccessDeniedException(
                    "Solamente el anfitrión propietario o un administrador pueden modificar esta configuración."
            );
        }

        return false;
    }

    private void validarIdentificadores(
            UUID idActor,
            UUID idPublicacion) {

        if (idActor == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación."
            );
        }

        if (idPublicacion == null) {
            throw new IllegalArgumentException(
                    "El identificador de la publicación es obligatorio."
            );
        }
    }

    private String normalizarModoExistente(String modo) {

        if (modo == null || modo.isBlank()) {
            return null;
        }

        try {

            return ModoAceptacionReserva.desde(modo).name();

        } catch (IllegalArgumentException e) {

            throw new IllegalStateException(
                    "La publicación tiene un modo de aceptación inválido.",
                    e
            );
        }
    }

    private ModoAceptacionPublicacionResponse construirRespuesta(
            Publicacion publicacion) {

        return new ModoAceptacionPublicacionResponse(
                publicacion.getIdPublicacion(),
                publicacion.getModoAceptacion(),
                publicacion.getFechaActualizacion()
        );
    }
}
