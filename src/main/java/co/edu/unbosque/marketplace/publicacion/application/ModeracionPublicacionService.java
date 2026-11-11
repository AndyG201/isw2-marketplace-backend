
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.ModeracionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAdministrativaResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDesbloqueadaEvent;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.RevisionPublicacionRepository;

@Service
public class ModeracionPublicacionService {

    private static final String BLOQUEADA = "BLOQUEADA";
    private static final String PAUSADA = "PAUSADA";

    private final PublicacionRepository publicacionRepository;
    private final RevisionPublicacionRepository revisionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final ApplicationEventPublisher eventPublisher;

    public ModeracionPublicacionService(
            PublicacionRepository publicacionRepository,
            RevisionPublicacionRepository revisionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            ApplicationEventPublisher eventPublisher) {

        this.publicacionRepository = publicacionRepository;
        this.revisionRepository = revisionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.eventPublisher = eventPublisher;
    }

    // CONSULTAR PUBLICACIONES

    @Transactional(readOnly = true)
    public List<PublicacionAdministrativaResponse> consultarPublicaciones() {

        return publicacionRepository.findAll(
                Sort.by(Sort.Direction.DESC, "fechaCreacion")
        ).stream()
                .map(publicacion ->
                        new PublicacionAdministrativaResponse(
                                publicacion.getIdPublicacion(),
                                publicacion.getIdAlojamiento(),
                                publicacion.getTitulo(),
                                publicacion.getEstado(),
                                publicacion.getFechaCreacion(),
                                publicacion.getFechaActualizacion()
                        )
                )
                .toList();
    }

    // BLOQUEAR PUBLICACION

    @Transactional
    public ModeracionPublicacionResponse bloquearPublicacion(
            UUID idAdministrador,
            UUID idPublicacion) {

        validarAdministrador(idAdministrador);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        if (BLOQUEADA.equals(publicacion.getEstado())) {
            throw new IllegalStateException(
                    "La publicación ya se encuentra BLOQUEADA."
            );
        }

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setEstado(BLOQUEADA);
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        RevisionPublicacion revision = registrarRevision(
                idAdministrador,
                idPublicacion,
                "BLOQUEAR_PUBLICACION",
                BLOQUEADA,
                null,
                ahora
        );

        return new ModeracionPublicacionResponse(
                revision.getIdRevision(),
                idPublicacion,
                revision.getAccion(),
                estadoAnterior,
                BLOQUEADA,
                null,
                "La publicación ha sido bloqueada correctamente.",
                ahora
        );
    }

    // DESBLOQUEAR PUBLICACION

    @Transactional
    public ModeracionPublicacionResponse desbloquearPublicacion(
            UUID idAdministrador,
            UUID idPublicacion,
            String motivo) {

        validarAdministrador(idAdministrador);

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes proporcionar el motivo del desbloqueo."
            );
        }

        String motivoFinal = motivo.trim();

        if (motivoFinal.length() > 2000) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar los 2000 caracteres."
            );
        }

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        if (!BLOQUEADA.equals(publicacion.getEstado())) {
            throw new IllegalStateException(
                    "Solamente puedes desbloquear publicaciones BLOQUEADAS."
            );
        }

        UUID idAnfitrion = alojamientoQuery
                .consultarIdAnfitrion(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el anfitrión del alojamiento."
                        )
                );

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setEstado(PAUSADA);
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        RevisionPublicacion revision = registrarRevision(
                idAdministrador,
                idPublicacion,
                "DESBLOQUEAR_PUBLICACION",
                "DESBLOQUEADA",
                motivoFinal,
                ahora
        );

        eventPublisher.publishEvent(
                new PublicacionDesbloqueadaEvent(
                        idPublicacion,
                        idAnfitrion,
                        publicacion.getTitulo(),
                        motivoFinal,
                        ahora
                )
        );

        return new ModeracionPublicacionResponse(
                revision.getIdRevision(),
                idPublicacion,
                revision.getAccion(),
                estadoAnterior,
                PAUSADA,
                motivoFinal,
                "La publicación ha sido desbloqueada y permanece PAUSADA.",
                ahora
        );
    }

    // VALIDACIONES

    private void validarAdministrador(UUID idAdministrador) {

        if (idAdministrador == null) {
            throw new IllegalArgumentException(
                    "No se pudo identificar al administrador."
            );
        }
    }

    private Publicacion obtenerPublicacion(UUID idPublicacion) {

        if (idPublicacion == null) {
            throw new IllegalArgumentException(
                    "Debes proporcionar el identificador de la publicación."
            );
        }

        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La publicación no existe."
                        )
                );
    }

    // REGISTRAR HISTORIAL ADMINISTRATIVO

    private RevisionPublicacion registrarRevision(
            UUID idAdministrador,
            UUID idPublicacion,
            String accion,
            String resultado,
            String motivo,
            OffsetDateTime fecha) {

        RevisionPublicacion revision = new RevisionPublicacion();

        revision.setIdRevision(UUID.randomUUID());
        revision.setIdPublicacion(idPublicacion);
        revision.setIdAdministrador(idAdministrador);
        revision.setAccion(accion);
        revision.setResultado(resultado);
        revision.setMotivo(motivo);
        revision.setFechaRevision(fecha);

        return revisionRepository.save(revision);
    }
}
