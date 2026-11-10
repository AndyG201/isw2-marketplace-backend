
package co.edu.unbosque.marketplace.usuario.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;

@Service
public class UsuarioPublicacionQueryService
        implements UsuarioPublicacionQuery {

    private final UsuarioRepository usuarioRepository;

    public UsuarioPublicacionQueryService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<UsuarioPublicacionInfo> consultarParaPublicacion(
            UUID idUsuario) {

        if (idUsuario == null) {
            return Optional.empty();
        }

        return usuarioRepository.findById(idUsuario)
                .map(this::convertirInformacion);
    }

    private UsuarioPublicacionInfo convertirInformacion(
            Usuario usuario) {

        boolean esAnfitrion =
                usuario.getRol() != null
                && usuario.getRol().getNombre() != null
                && "ANFITRION".equalsIgnoreCase(
                        usuario.getRol().getNombre().trim()
                );

        boolean cuentaActiva =
                usuario.getEstado() == EstadoUsuario.ACTIVA;

        boolean correoVerificado =
                usuario.isCorreoVerificado();

        boolean restringido =
                usuario.isRestringido();

        int intentosFallidos =
                usuario.getIntentosFallidos();

        boolean cuentaHabilitada =
                cuentaActiva
                && correoVerificado
                && !restringido
                && intentosFallidos < 5;

        return new UsuarioPublicacionInfo(
                esAnfitrion,
                cuentaHabilitada,
                cuentaActiva,
                restringido,
                correoVerificado,
                intentosFallidos
        );
    }
}
