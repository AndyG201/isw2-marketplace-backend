
package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.usuario.api.request.RestringirUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.api.response.AdministradorUsuarioResponse;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@Service
public class AdministradorService {

	private final UsuarioRepository usuarioRepository;
	private final JwtUtil jwtUtil;

	public AdministradorService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil) {

		this.usuarioRepository = usuarioRepository;
		this.jwtUtil = jwtUtil;
	}

	public void reactivarBloqueo(String correo) {

		if (correo == null || correo.isBlank()) {
			throw new IllegalArgumentException("Debes proporcionar el correo del usuario.");
		}

		Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		if (usuario.getIntentosFallidos() < 5
				|| !"Bloqueo automático por intentos fallidos".equals(usuario.getMotivoBloqueo())) {

			throw new IllegalStateException("La cuenta no tiene un bloqueo automático pendiente de reactivación.");
		}

		usuario.setIntentosFallidos(0);
		usuario.setMotivoBloqueo(null);
		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);
	}

	@Transactional
	public void restringirUsuario(UUID idUsuario, RestringirUsuarioRequest solicitud) {

		if (idUsuario == null || solicitud == null || solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()
				|| solicitud.getMotivo().trim().length() > 255) {

			throw new IllegalArgumentException("Debes proporcionar un motivo válido para la restricción.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		if (usuario.isRestringido()) {
			throw new IllegalStateException("La cuenta ya tiene una restricción administrativa.");
		}

		usuario.setRestringido(true);
		usuario.setMotivoRestriccion(solicitud.getMotivo().trim());
		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);

		jwtUtil.invalidarSesiones(idUsuario);
	}

	@Transactional
	public void levantarRestriccion(UUID idUsuario) {

		if (idUsuario == null) {
			throw new IllegalArgumentException("Debes proporcionar el identificador del usuario.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		if (!usuario.isRestringido()) {
			throw new IllegalStateException("La cuenta no tiene una restricción administrativa vigente.");
		}

		usuario.setRestringido(false);
		usuario.setMotivoRestriccion(null);
		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);
	}

	public List<AdministradorUsuarioResponse> consultarUsuarios() {

		return usuarioRepository.findAll().stream()
				.map(usuario -> new AdministradorUsuarioResponse(usuario.getIdUsuario(), usuario.getNombre(),
						usuario.getApellido(), usuario.getCorreo(),
						usuario.getRol() != null ? usuario.getRol().getNombre() : null,
						usuario.getEstado() != null ? usuario.getEstado().name() : null, usuario.isRestringido(),
						usuario.getMotivoRestriccion()))
				.toList();
	}

}
