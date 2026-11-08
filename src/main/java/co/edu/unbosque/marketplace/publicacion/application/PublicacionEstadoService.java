
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.publicacion.api.response.EstadoPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@Service
public class PublicacionEstadoService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoRepository alojamientoRepository;

    public PublicacionEstadoService(
            PublicacionRepository publicacionRepository,
            AlojamientoRepository alojamientoRepository) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoRepository = alojamientoRepository;
    }

    @Transactional
    public EstadoPublicacionResponse pausarPublicacion(
            Usuario anfitrion,
            UUID idPublicacion) {

        validarAnfitrion(anfitrion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        Alojamiento alojamiento = obtenerAlojamiento(publicacion);

        validarPropiedad(anfitrion, alojamiento);

        validarNoBloqueada(publicacion);

        if (!"ACTIVA".equals(publicacion.getEstado())) {

            throw new IllegalStateException(
                    "Solamente puedes pausar publicaciones que se encuentren ACTIVA."
            );
        }

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setEstado("PAUSADA");
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        return new EstadoPublicacionResponse(
                publicacion.getIdPublicacion(),
                estadoAnterior,
                publicacion.getEstado(),
                "La publicación ha sido pausada correctamente.",
                ahora
        );
    }

    @Transactional
    public EstadoPublicacionResponse reactivarPublicacion(
            Usuario anfitrion,
            UUID idPublicacion) {

        validarAnfitrion(anfitrion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        Alojamiento alojamiento = obtenerAlojamiento(publicacion);

        validarPropiedad(anfitrion, alojamiento);

        validarNoBloqueada(publicacion);

        if (!"PAUSADA".equals(publicacion.getEstado())) {

            throw new IllegalStateException(
                    "Solamente puedes reactivar publicaciones que se encuentren PAUSADA."
            );
        }

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setEstado("ACTIVA");
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        return new EstadoPublicacionResponse(
                publicacion.getIdPublicacion(),
                estadoAnterior,
                publicacion.getEstado(),
                "La publicación ha sido reactivada correctamente.",
                ahora
        );
    }

    private void validarAnfitrion(Usuario anfitrion) {

        if (anfitrion == null
                || anfitrion.getIdUsuario() == null) {

            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación."
            );
        }

        if (anfitrion.getRol() == null
                || !"ANFITRION".equalsIgnoreCase(
                        anfitrion.getRol().getNombre())) {

            throw new AccessDeniedException(
                    "Solamente un anfitrión puede pausar o reactivar publicaciones."
            );
        }

        if (!anfitrion.isCorreoVerificado()
                || anfitrion.getEstado() != EstadoUsuario.ACTIVA
                || anfitrion.isRestringido()
                || anfitrion.getIntentosFallidos() >= 5) {

            throw new AccessDeniedException(
                    "Tu cuenta no está habilitada para administrar publicaciones."
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
                        ));
    }

    private Alojamiento obtenerAlojamiento(Publicacion publicacion) {

        return alojamientoRepository
                .findById(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado a la publicación."
                        ));
    }

    private void validarPropiedad(
            Usuario anfitrion,
            Alojamiento alojamiento) {

        if (!anfitrion.getIdUsuario().equals(
                alojamiento.getIdAnfitrion())) {

            throw new AccessDeniedException(
                    "No puedes modificar una publicación que no te pertenece."
            );
        }
    }

    private void validarNoBloqueada(Publicacion publicacion) {

        if ("BLOQUEADA".equals(publicacion.getEstado())) {

            throw new IllegalStateException(
                    "La publicación está BLOQUEADA por un Administrador "
                    + "y no puede pausarse ni reactivarse."
            );
        }
    }
}
