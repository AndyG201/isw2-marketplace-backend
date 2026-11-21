package co.edu.unbosque.marketplace.usuario.application;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.usuario.application.port.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionQuery;

@Service
public class UsuarioSesionQueryService implements UsuarioSesionQuery {

    private final UsuarioRepository usuarios;

    public UsuarioSesionQueryService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioSesionInfo> consultarPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return Optional.empty();
        }

        return usuarios.findByCorreoIgnoreCase(correo.trim())
                .map(usuario -> new UsuarioSesionInfo(
                        usuario.getIdUsuario(),
                        usuario.getRol() != null
                                && usuario.getRol().getNombre() != null
                                && "ADMINISTRADOR".equals(
                                        usuario.getRol().getNombre()
                                                .trim()
                                                .toUpperCase(Locale.ROOT))));
    }
}