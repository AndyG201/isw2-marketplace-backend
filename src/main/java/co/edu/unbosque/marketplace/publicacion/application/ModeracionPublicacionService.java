
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Sort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.application.dto.ModeracionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionAdministrativaResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDesbloqueadaEvent;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.RevisionPublicacionRepository;

@Service
public class ModeracionPublicacionService {


    private final PublicacionRepository publicacionRepository;
    private final RevisionPublicacionRepository revisionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final ApplicationEventPublisher eventPublisher;
    private AuditoriaEventoCommand auditoria;

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

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
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
            UUID idPublicacion,
            String motivo) {

        validarAdministrador(idAdministrador);

        String motivoFinal = validarMotivo(motivo);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        if (publicacion.estaEn(EstadoPublicacion.BLOQUEADA)) {
            throw new ConflictoNegocioException(
                    "La publicación ya se encuentra BLOQUEADA."
            );
        }

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.cambiarEstado(EstadoPublicacion.BLOQUEADA);
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        RevisionPublicacion revision = registrarRevision(
                idAdministrador,
                idPublicacion,
                "BLOQUEAR_PUBLICACION",
                EstadoPublicacion.BLOQUEADA.name(),
                motivoFinal,
                ahora
        );

        registrarEventoModeracion(idAdministrador, idPublicacion, estadoAnterior, EstadoPublicacion.BLOQUEADA.name(), "BLOQUEAR_PUBLICACION", ahora);

        return new ModeracionPublicacionResponse(
                revision.getIdRevision(),
                idPublicacion,
                revision.getAccion(),
                estadoAnterior,
                EstadoPublicacion.BLOQUEADA.name(),
                motivoFinal,
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

        String motivoFinal = validarMotivo(motivo);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        if (!publicacion.estaEn(EstadoPublicacion.BLOQUEADA)) {
            throw new ConflictoNegocioException(
                    "Solamente puedes desbloquear publicaciones BLOQUEADAS."
            );
        }

        UUID idAnfitrion = alojamientoQuery
                .consultarIdAnfitrion(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new ConflictoNegocioException(
                                "No se encontró el anfitrión del alojamiento."
                        )
                );

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.cambiarEstado(EstadoPublicacion.PAUSADA);
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

        registrarEventoModeracion(idAdministrador, idPublicacion, estadoAnterior, EstadoPublicacion.PAUSADA.name(), "DESBLOQUEAR_PUBLICACION", ahora);

        // Notificar al módulo de notificaciones.

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
                EstadoPublicacion.PAUSADA.name(),
                motivoFinal,
                "La publicación ha sido desbloqueada y permanece PAUSADA.",
                ahora
        );
    }

    private void registrarEventoModeracion(
            UUID idAdministrador, UUID idPublicacion, String estadoAnterior,
            String estadoNuevo, String accion, OffsetDateTime fecha) {
        if (auditoria != null) {
            auditoria.registrarEvento(new RegistrarEventoAuditoria(
                    idAdministrador, fecha, accion, "EXITOSO", "PUBLICACION",
                    idPublicacion,
                    "{\"estadoAnterior\":\"" + estadoAnterior
                            + "\",\"estadoNuevo\":\"" + estadoNuevo + "\"}", null));
        }
    }

    // VALIDAR ADMINISTRADOR

    private void validarAdministrador(UUID idAdministrador) {

        if (idAdministrador == null) {
            throw new DatosInvalidosException(
                    "No se pudo identificar al administrador."
            );
        }
    }

    // VALIDAR MOTIVO

    private String validarMotivo(String motivo) {

        if (motivo == null || motivo.isBlank()) {
            throw new DatosInvalidosException(
                    "Debes proporcionar el motivo de la operación."
            );
        }

        String motivoFinal = motivo.trim();

        if (motivoFinal.length() > 2000) {
            throw new DatosInvalidosException(
                    "El motivo no puede superar los 2000 caracteres."
            );
        }

        return motivoFinal;
    }

    // OBTENER PUBLICACION

    private Publicacion obtenerPublicacion(UUID idPublicacion) {

        if (idPublicacion == null) {
            throw new DatosInvalidosException(
                    "Debes proporcionar el identificador de la publicación."
            );
        }

        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
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
