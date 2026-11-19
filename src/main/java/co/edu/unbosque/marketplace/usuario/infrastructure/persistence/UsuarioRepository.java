package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByCorreo(String correo);

	boolean existsByDocumentoHash(byte[] documentoHash);

	boolean existsByTelefono(String telefono);

	Optional<Usuario> findByCorreoIgnoreCase(String correo);
	
	boolean existsByCorreoIgnoreCaseAndIdUsuarioNot(
	        String correo,
	        UUID idUsuario
	);

	@Modifying
	@Query("""
	        delete from Usuario u
	        where u.estado = :estado
	          and u.correoVerificado = false
	          and u.fechaRegistro < :limite
	        """)
	int eliminarSinVerificarRegistradosAntesDe(
	        @Param("estado") EstadoUsuario estado,
	        @Param("limite") OffsetDateTime limite
	);
}
