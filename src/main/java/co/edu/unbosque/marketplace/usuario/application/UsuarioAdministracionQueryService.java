
package co.edu.unbosque.marketplace.usuario.application;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;

@Service
public class UsuarioAdministracionQueryService
        implements UsuarioAdministracionQuery {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAdministracionQueryService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean esAdministrador(UUID idUsuario) {

        if (idUsuario == null) {
            return false;
        }

        return usuarioRepository.findById(idUsuario)
                .map(this::tieneRolAdministrador)
                .orElse(false);
    }

    private boolean tieneRolAdministrador(Usuario usuario) {

        return usuario.getRol() != null
                && usuario.getRol().getNombre() != null
                && "ADMINISTRADOR".equals(
                        usuario.getRol().getNombre()
                                .trim()
                                .toUpperCase(Locale.ROOT)
                );
    }
}
