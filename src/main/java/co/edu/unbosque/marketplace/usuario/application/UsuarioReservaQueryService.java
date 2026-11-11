package co.edu.unbosque.marketplace.usuario.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.usuario.contracts.UsuarioReservaInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioReservaQuery;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;

@Service
public class UsuarioReservaQueryService implements UsuarioReservaQuery {

    private final UsuarioRepository usuarioRepository;

    public UsuarioReservaQueryService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioReservaInfo> consultarDatosHuesped(
            UUID idHuesped) {

        if (idHuesped == null) {
            return Optional.empty();
        }

        return usuarioRepository.findById(idHuesped)
                .map(this::convertirInformacion);
    }

    private UsuarioReservaInfo convertirInformacion(Usuario usuario) {

        String nombre = usuario.getNombre() == null
                ? ""
                : usuario.getNombre().trim();

        String apellido = usuario.getApellido() == null
                ? ""
                : usuario.getApellido().trim();

        String nombreCompleto = (nombre + " " + apellido).trim();

        if (nombreCompleto.isBlank()) {
            nombreCompleto = "Huésped sin nombre registrado";
        }

        return new UsuarioReservaInfo(
                usuario.getIdUsuario(),
                nombreCompleto
        );
    }
}
