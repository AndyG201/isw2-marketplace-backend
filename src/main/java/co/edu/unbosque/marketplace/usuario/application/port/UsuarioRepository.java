package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

public interface UsuarioRepository {

    <S extends Usuario> S save(S entidad);
    <S extends Usuario> List<S> saveAll(Iterable<S> entidades);
    <S extends Usuario> S saveAndFlush(S entidad);
    Optional<Usuario> findById(UUID id);
    List<Usuario> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Usuario entidad);
    void deleteById(UUID id);
    void flush();
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByDocumentoHash(byte[] documentoHash);
    boolean existsByTelefono(String telefono);
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(String correo, UUID idUsuario);
    int eliminarSinVerificarRegistradosAntesDe(EstadoUsuario estado, OffsetDateTime limite);
    List<UUID> buscarIdsAdministradores(EstadoUsuario estado);
}
