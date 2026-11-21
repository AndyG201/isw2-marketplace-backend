
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.application.dto.DecisionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class RevisionAdministrativaService {

    private static final String PAUSADA = "PAUSADA";

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final UsuarioAdministracionQuery usuarioAdministracionQuery;
    private final UsuarioPublicacionQuery usuarioPublicacionQuery;
    private final RevisionPublicacionRepository revisionRepository;
    private final CambiosPublicacionService cambios;
    private AuditoriaEventoCommand auditoria;

    public RevisionAdministrativaService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            UsuarioAdministracionQuery usuarioAdministracionQuery,
            UsuarioPublicacionQuery usuarioPublicacionQuery,
            RevisionPublicacionRepository revisionRepository,
            CambiosPublicacionService cambios) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.usuarioAdministracionQuery = usuarioAdministracionQuery;
        this.usuarioPublicacionQuery = usuarioPublicacionQuery;
        this.revisionRepository = revisionRepository;
        this.cambios = cambios;
    }

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
    }

    @Transactional
    public DecisionPublicacionResponse aprobarPublicacion(
            UUID idAdministrador,
            UUID idPublicacion) {

        validarAdministrador(idAdministrador);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarEstadoRevision(publicacion);

        UUID idAnfitrion = alojamientoQuery
                .consultarIdAnfitrion(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado a la publicación."));

        UsuarioPublicacionInfo anfitrion = usuarioPublicacionQuery
                .consultarParaPublicacion(idAnfitrion)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró la cuenta del anfitrión."));

        validarCuentaAnfitrion(anfitrion);

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        String estadoFinal = cambios.resolver(publicacion.getIdPublicacion(), ahora)
                .filter(PAUSADA::equals)
                .orElse("ACTIVA");

        publicacion.setEstado(estadoFinal);
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        RevisionPublicacion revision = registrarRevision(
                idAdministrador,
                publicacion.getIdPublicacion(),
                "APROBAR_PUBLICACION",
                "APROBADA",
                null,
                ahora
        );
        registrarEventoRevision(idAdministrador, publicacion, estadoAnterior, estadoFinal, "APROBAR_PUBLICACION", ahora);

        return new DecisionPublicacionResponse(
                revision.getIdRevision(),
                publicacion.getIdPublicacion(),
                revision.getAccion(),
                estadoAnterior,
                publicacion.getEstado(),
                null,
                PAUSADA.equals(estadoFinal)
                        ? "Los cambios fueron aprobados y la publicación sigue pausada, como la dejó el anfitrión."
                        : "La publicación ha sido aprobada y activada correctamente.",
                ahora
        );
    }

    @Transactional
    public DecisionPublicacionResponse rechazarPublicacion(
            UUID idAdministrador,
            UUID idPublicacion,
            String motivo) {

        validarAdministrador(idAdministrador);

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "Debes proporcionar el motivo del rechazo.");
        }

        if (motivo.trim().length() > 2000) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar los 2000 caracteres.");
        }

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarEstadoRevision(publicacion);

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        String motivoFinal = motivo.trim();

        cambios.resolver(publicacion.getIdPublicacion(), ahora);

        publicacion.setEstado("BORRADOR");
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        RevisionPublicacion revision = registrarRevision(
                idAdministrador,
                publicacion.getIdPublicacion(),
                "RECHAZAR_PUBLICACION",
                "RECHAZADA",
                motivoFinal,
                ahora
        );
        registrarEventoRevision(idAdministrador, publicacion, estadoAnterior, "BORRADOR", "RECHAZAR_PUBLICACION", ahora);

        return new DecisionPublicacionResponse(
                revision.getIdRevision(),
                publicacion.getIdPublicacion(),
                revision.getAccion(),
                estadoAnterior,
                publicacion.getEstado(),
                motivoFinal,
                "La publicación ha sido rechazada y devuelta a BORRADOR.",
                ahora
        );
    }

    private void registrarEventoRevision(
            UUID idAdministrador, Publicacion publicacion, String estadoAnterior,
            String estadoNuevo, String accion, OffsetDateTime fecha) {
        if (auditoria != null) {
            auditoria.registrarEvento(new RegistrarEventoAuditoria(
                    idAdministrador, fecha, accion, "EXITOSO", "PUBLICACION",
                    publicacion.getIdPublicacion(),
                    "{\"estadoAnterior\":\"" + estadoAnterior
                            + "\",\"estadoNuevo\":\"" + estadoNuevo + "\"}", null));
        }
    }

    private void validarAdministrador(UUID idAdministrador) {

        if (idAdministrador == null
                || !usuarioAdministracionQuery.esAdministrador(idAdministrador)) {

            throw new AccessDeniedException(
                    "Solo un Administrador puede aprobar o rechazar publicaciones.");
        }
    }

    private Publicacion obtenerPublicacion(UUID idPublicacion) {

        if (idPublicacion == null) {
            throw new IllegalArgumentException(
                    "Debes proporcionar el identificador de la publicación.");
        }

        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "La publicación no existe."));
    }

    private void validarEstadoRevision(Publicacion publicacion) {

        if (!"PENDIENTE_REVISION".equals(publicacion.getEstado())) {

            throw new IllegalStateException(
                    "No se puede aprobar o rechazar la publicación "
                    + "porque no se encuentra en estado PENDIENTE_REVISION.");
        }
    }

    private void validarCuentaAnfitrion(
            UsuarioPublicacionInfo anfitrion) {

        if (!anfitrion.cuentaActiva()) {
            throw new IllegalStateException(
                    "No se puede activar la publicación porque la cuenta "
                    + "del anfitrión está desactivada.");
        }

        if (anfitrion.restringido()) {
            throw new IllegalStateException(
                    "No se puede activar la publicación porque la cuenta "
                    + "del anfitrión tiene una restricción administrativa.");
        }

        if (!anfitrion.correoVerificado()) {
            throw new IllegalStateException(
                    "No se puede activar la publicación porque el correo "
                    + "del anfitrión no está verificado.");
        }

        if (anfitrion.intentosFallidos() >= 5) {
            throw new IllegalStateException(
                    "No se puede activar la publicación porque la cuenta "
                    + "del anfitrión está bloqueada por intentos fallidos.");
        }

        if (!anfitrion.esAnfitrion()) {
            throw new IllegalStateException(
                    "La cuenta propietaria ya no tiene el rol de ANFITRION.");
        }
    }

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
