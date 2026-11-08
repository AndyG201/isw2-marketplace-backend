
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.publicacion.api.response.DecisionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;

@Service
public class RevisionAdministrativaService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RevisionPublicacionRepository revisionRepository;

    public RevisionAdministrativaService(
            PublicacionRepository publicacionRepository,
            AlojamientoRepository alojamientoRepository,
            UsuarioRepository usuarioRepository,
            RevisionPublicacionRepository revisionRepository) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.revisionRepository = revisionRepository;
    }

    @Transactional
    public DecisionPublicacionResponse aprobarPublicacion(
            Usuario administrador,
            UUID idPublicacion) {

        validarAdministrador(administrador);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarEstadoRevision(publicacion);

        Alojamiento alojamiento = obtenerAlojamiento(publicacion);

        Usuario anfitrion = usuarioRepository
                .findById(alojamiento.getIdAnfitrion())
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
                administrador.getIdUsuario(),
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
            Usuario administrador,
            UUID idPublicacion,
            String motivo) {

        validarAdministrador(administrador);

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
                administrador.getIdUsuario(),
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

    private void validarAdministrador(Usuario administrador) {

        if (administrador == null
                || administrador.getIdUsuario() == null) {

            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación.");
        }

        String rol = administrador.getRol() == null
                ? ""
                : administrador.getRol().getNombre()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        if (!"ADMINISTRADOR".equals(rol)) {

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

    private Alojamiento obtenerAlojamiento(Publicacion publicacion) {

        return alojamientoRepository
                .findById(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado a la publicación."));
    }

    private void validarEstadoRevision(Publicacion publicacion) {

        if (!"PENDIENTE_REVISION".equals(publicacion.getEstado())) {

            throw new IllegalStateException(
                    "No se puede aprobar o rechazar la publicación "
                    + "porque no se encuentra en estado PENDIENTE_REVISION.");
        }
    }

    private void validarCuentaAnfitrion(Usuario anfitrion) {

        if (anfitrion.getEstado() != EstadoUsuario.ACTIVA) {

            throw new IllegalStateException(
                    "No se puede activar la publicación porque la cuenta "
                    + "del anfitrión está desactivada.");
        }

        if (anfitrion.isRestringido()) {

            throw new IllegalStateException(
                    "No se puede activar la publicación porque la cuenta "
                    + "del anfitrión tiene una restricción administrativa.");
        }

        if (!anfitrion.isCorreoVerificado()) {

            throw new IllegalStateException(
                    "No se puede activar la publicación porque el correo "
                    + "del anfitrión no está verificado.");
        }

        if (anfitrion.getIntentosFallidos() >= 5) {

            throw new IllegalStateException(
                    "No se puede activar la publicación porque la cuenta "
                    + "del anfitrión está bloqueada por intentos fallidos.");
        }

        if (anfitrion.getRol() == null
                || !"ANFITRION".equalsIgnoreCase(
                        anfitrion.getRol().getNombre())) {

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
