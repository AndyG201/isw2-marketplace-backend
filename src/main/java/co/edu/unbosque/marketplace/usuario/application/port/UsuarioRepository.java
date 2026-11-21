package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

public interface UsuarioRepository extends RepositorioBasico<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByDocumentoHash(byte[] documentoHash);
    boolean existsByTelefono(String telefono);
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, UUID idUsuario);
    int eliminarSinVerificarRegistradosAntesDe(EstadoUsuario estado, OffsetDateTime limite);
    List<UUID> buscarIdsAdministradores(EstadoUsuario estado);
}
