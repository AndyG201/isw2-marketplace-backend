
package co.edu.unbosque.marketplace.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.model.VerificacionCorreo;
import co.edu.unbosque.marketplace.model.Usuario;

@Repository
public interface VerificacionCorreoRepository extends JpaRepository<VerificacionCorreo, UUID> {

    Optional<VerificacionCorreo> findByUsuarioAndVerificadoFalse(Usuario usuario);
    Optional<VerificacionCorreo>findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(Usuario usuario);

}
