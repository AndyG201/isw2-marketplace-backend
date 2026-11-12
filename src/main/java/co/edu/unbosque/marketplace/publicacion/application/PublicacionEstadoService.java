
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.EstadoPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class PublicacionEstadoService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoPublicacionQuery;
    private final UsuarioPublicacionQuery usuarioPublicacionQuery;

    public PublicacionEstadoService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoPublicacionQuery,
            UsuarioPublicacionQuery usuarioPublicacionQuery) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoPublicacionQuery = alojamientoPublicacionQuery;
        this.usuarioPublicacionQuery = usuarioPublicacionQuery;
    }

    @Transactional
    public EstadoPublicacionResponse pausarPublicacion(
            UUID idAnfitrion,
            UUID idPublicacion) {

        validarAnfitrion(idAnfitrion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarPropiedad(idAnfitrion, publicacion);

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
            UUID idAnfitrion,
            UUID idPublicacion) {

        validarAnfitrion(idAnfitrion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarPropiedad(idAnfitrion, publicacion);

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

    private void validarAnfitrion(UUID idAnfitrion) {

        if (idAnfitrion == null) {

            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación."
            );
        }

        UsuarioPublicacionInfo anfitrion =
                usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Debes iniciar sesión para realizar esta operación."
                                ));

        if (!anfitrion.esAnfitrion()) {

            throw new AccessDeniedException(
                    "Solamente un anfitrión puede pausar o reactivar publicaciones."
            );
        }

        if (!anfitrion.cuentaHabilitadaParaPublicar()) {

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

    private void validarPropiedad(
            UUID idAnfitrion,
            Publicacion publicacion) {

        UUID idPropietario = alojamientoPublicacionQuery
                .consultarIdAnfitrion(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado a la publicación."
                        ));

        if (!idAnfitrion.equals(idPropietario)) {

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
