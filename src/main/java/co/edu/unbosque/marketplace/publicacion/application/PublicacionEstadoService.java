
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.application.dto.EstadoPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class PublicacionEstadoService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoPublicacionQuery;
    private final UsuarioPublicacionQuery usuarioPublicacionQuery;
    private AuditoriaEventoCommand auditoria;

    public PublicacionEstadoService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoPublicacionQuery,
            UsuarioPublicacionQuery usuarioPublicacionQuery) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoPublicacionQuery = alojamientoPublicacionQuery;
        this.usuarioPublicacionQuery = usuarioPublicacionQuery;
    }

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
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

            throw new ConflictoNegocioException(
                    "Solamente puedes pausar publicaciones que se encuentren ACTIVA."
            );
        }

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setEstado("PAUSADA");
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);
        registrarEventoEstado(idAnfitrion, publicacion, estadoAnterior, "PAUSAR_PUBLICACION", ahora);

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

            throw new ConflictoNegocioException(
                    "Solamente puedes reactivar publicaciones que se encuentren PAUSADA."
            );
        }

        String estadoAnterior = publicacion.getEstado();

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setEstado("ACTIVA");
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);
        registrarEventoEstado(idAnfitrion, publicacion, estadoAnterior, "REACTIVAR_PUBLICACION", ahora);

        return new EstadoPublicacionResponse(
                publicacion.getIdPublicacion(),
                estadoAnterior,
                publicacion.getEstado(),
                "La publicación ha sido reactivada correctamente.",
                ahora
        );
    }

    private void registrarEventoEstado(
            UUID idAnfitrion, Publicacion publicacion, String estadoAnterior,
            String accion, OffsetDateTime fecha) {
        if (auditoria != null) {
            auditoria.registrarEvento(new RegistrarEventoAuditoria(
                    idAnfitrion, fecha, accion, "EXITOSO", "PUBLICACION",
                    publicacion.getIdPublicacion(),
                    "{\"estadoAnterior\":\"" + estadoAnterior
                            + "\",\"estadoNuevo\":\"" + publicacion.getEstado() + "\"}", null));
        }
    }

    private void validarAnfitrion(UUID idAnfitrion) {

        if (idAnfitrion == null) {

            throw new AccesoNoPermitidoException(
                    "Debes iniciar sesión para realizar esta operación."
            );
        }

        UsuarioPublicacionInfo anfitrion =
                usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion)
                        .orElseThrow(() ->
                                new AccesoNoPermitidoException(
                                        "Debes iniciar sesión para realizar esta operación."
                                ));

        if (!anfitrion.esAnfitrion()) {

            throw new AccesoNoPermitidoException(
                    "Solamente un anfitrión puede pausar o reactivar publicaciones."
            );
        }

        if (!anfitrion.cuentaHabilitadaParaPublicar()) {

            throw new AccesoNoPermitidoException(
                    "Tu cuenta no está habilitada para administrar publicaciones."
            );
        }
    }

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
                        ));
    }

    private void validarPropiedad(
            UUID idAnfitrion,
            Publicacion publicacion) {

        UUID idPropietario = alojamientoPublicacionQuery
                .consultarIdAnfitrion(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new ConflictoNegocioException(
                                "No se encontró el alojamiento asociado a la publicación."
                        ));

        if (!idAnfitrion.equals(idPropietario)) {

            throw new AccesoNoPermitidoException(
                    "No puedes modificar una publicación que no te pertenece."
            );
        }
    }

    private void validarNoBloqueada(Publicacion publicacion) {

        if ("BLOQUEADA".equals(publicacion.getEstado())) {

            throw new ConflictoNegocioException(
                    "La publicación está BLOQUEADA por un Administrador "
                    + "y no puede pausarse ni reactivarse."
            );
        }
    }
}
