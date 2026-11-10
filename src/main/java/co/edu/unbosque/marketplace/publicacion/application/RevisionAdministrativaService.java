
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.DecisionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class RevisionAdministrativaService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final UsuarioAdministracionQuery usuarioAdministracionQuery;
    private final UsuarioPublicacionQuery usuarioPublicacionQuery;
    private final RevisionPublicacionRepository revisionRepository;

    public RevisionAdministrativaService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            UsuarioAdministracionQuery usuarioAdministracionQuery,
            UsuarioPublicacionQuery usuarioPublicacionQuery,
            RevisionPublicacionRepository revisionRepository) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.usuarioAdministracionQuery = usuarioAdministracionQuery;
        this.usuarioPublicacionQuery = usuarioPublicacionQuery;
        this.revisionRepository = revisionRepository;
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

        publicacion.setEstado("ACTIVA");
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

        return new DecisionPublicacionResponse(
                revision.getIdRevision(),
                publicacion.getIdPublicacion(),
                revision.getAccion(),
                estadoAnterior,
                publicacion.getEstado(),
                null,
                "La publicación ha sido aprobada y activada correctamente.",
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
