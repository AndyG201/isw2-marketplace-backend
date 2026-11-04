
package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.domain.VerificacionCorreo;

@Repository
public interface VerificacionCorreoRepository
        extends JpaRepository<VerificacionCorreo, UUID> {

    Optional<VerificacionCorreo>
            findByUsuarioAndVerificadoFalse(Usuario usuario);

    Optional<VerificacionCorreo>
            findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(
                    Usuario usuario
            );

    @Modifying
    @Query("""
        UPDATE VerificacionCorreo v
        SET v.fechaExpiracion = :fecha
        WHERE v.usuario = :usuario
        AND v.verificado = false
    """)
    int invalidarCodigosPendientes(
            @Param("usuario") Usuario usuario,
            @Param("fecha") OffsetDateTime fecha
    );
}
