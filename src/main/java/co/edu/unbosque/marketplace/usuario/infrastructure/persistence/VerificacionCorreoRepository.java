
package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.usuario.domein.Usuario;
import co.edu.unbosque.marketplace.usuario.domein.VerificacionCorreo;

@Repository
public interface VerificacionCorreoRepository extends JpaRepository<VerificacionCorreo, UUID> {

    Optional<VerificacionCorreo> findByUsuarioAndVerificadoFalse(Usuario usuario);
    Optional<VerificacionCorreo>findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(Usuario usuario);

}
