package co.edu.unbosque.marketplace.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
	
	Optional<Usuario> findByCorreo(String correo);

	boolean existsByDocumentoHash(byte[] documentoHash);

	boolean existsByTelefono(String telefono);
}
