package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.domain.VerificacionCorreo;

public interface VerificacionCorreoRepository extends RepositorioBasico<VerificacionCorreo, UUID> {
    Optional<VerificacionCorreo> findByUsuarioAndVerificadoFalse(Usuario usuario);
    Optional<VerificacionCorreo> findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(Usuario usuario);
    int invalidarCodigosPendientes(Usuario usuario, OffsetDateTime fecha);
}
