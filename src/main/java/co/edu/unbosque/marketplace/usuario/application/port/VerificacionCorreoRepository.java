package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.domain.VerificacionCorreo;

public interface VerificacionCorreoRepository {

    <S extends VerificacionCorreo> S save(S entidad);
    <S extends VerificacionCorreo> List<S> saveAll(Iterable<S> entidades);
    <S extends VerificacionCorreo> S saveAndFlush(S entidad);
    Optional<VerificacionCorreo> findById(UUID id);
    List<VerificacionCorreo> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(VerificacionCorreo entidad);
    void deleteById(UUID id);
    void flush();
    Optional<VerificacionCorreo> findByUsuarioAndVerificadoFalse(Usuario usuario);
    Optional<VerificacionCorreo> findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(Usuario usuario);
    int invalidarCodigosPendientes(Usuario usuario, OffsetDateTime fecha);
}
