
package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.usuario.application.dto.AdministradorUsuarioResponse;
import co.edu.unbosque.marketplace.usuario.application.dto.ModificarDocumentoRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.RestringirUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.application.port.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.contracts.CuentaRestringidaEvent;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;

@Service
public class AdministradorService {

	private static final String[] TIPOS_DOCUMENTO = { "CC", "CE", "TI", "PASAPORTE" };

	private final UsuarioRepository usuarioRepository;
	private final JwtUtil jwtUtil;
	private final ApplicationEventPublisher eventPublisher;
	private AuditoriaEventoCommand auditoria;
	private ProteccionDocumento proteccionDocumento;

	public AdministradorService(UsuarioRepository usuarioRepository, JwtUtil jwtUtil,
			ApplicationEventPublisher eventPublisher) {

		this.usuarioRepository = usuarioRepository;
		this.jwtUtil = jwtUtil;
		this.eventPublisher = eventPublisher;
	}

	@Autowired
	public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
		this.auditoria = auditoria;
	}

	@Autowired
	public void configurarProteccionDocumento(ProteccionDocumento proteccionDocumento) {
		this.proteccionDocumento = proteccionDocumento;
	}

	@Transactional
	public void modificarDocumento(UUID idUsuario, ModificarDocumentoRequest solicitud) {

		if (idUsuario == null || solicitud == null || solicitud.tipoDocumento() == null
				|| solicitud.documentoIdentidad() == null || solicitud.documentoIdentidad().isBlank()) {
			throw new DatosInvalidosException("Debes proporcionar el tipo y el número de documento.");
		}

		String tipo = solicitud.tipoDocumento().trim().toUpperCase(Locale.ROOT);
		if (Arrays.stream(TIPOS_DOCUMENTO).noneMatch(tipo::equals)) {
			throw new DatosInvalidosException("El tipo de documento debe ser CC, CE, TI o PASAPORTE.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new DatosInvalidosException("Usuario no encontrado."));

		ProteccionDocumento.DatosDocumento datos = proteccionDocumento
				.protegerDocumento(tipo, solicitud.documentoIdentidad());

		if (Arrays.equals(datos.documentoHash(), usuario.getDocumentoHash())) {
			throw new ConflictoNegocioException("La cuenta ya tiene registrado ese documento.");
		}

		if (usuarioRepository.existsByDocumentoHash(datos.documentoHash())) {
			throw new ConflictoNegocioException("Ya existe otra cuenta con ese documento.");
		}

		String tipoAnterior = usuario.getTipoDocumento();
		OffsetDateTime ahora = OffsetDateTime.now();

		usuario.setTipoDocumento(tipo);
		usuario.setDocumentoCifrado(datos.documentoCifrado());
		usuario.setDocumentoNonce(datos.documentoNonce());
		usuario.setDocumentoHash(datos.documentoHash());
		usuario.setFechaActualizacion(ahora);

		usuarioRepository.save(usuario);
		registrarAuditoria(actorActual(), idUsuario, "MODIFICAR_DOCUMENTO_USUARIO", ahora,
				"{\"campo\":\"DOCUMENTO\",\"tipoAnterior\":\"" + tipoAnterior
						+ "\",\"tipoNuevo\":\"" + tipo + "\"}");
	}

	@Transactional
	public void reactivarBloqueo(String correo) {

		if (correo == null || correo.isBlank()) {
			throw new RecursoNoEncontradoException("Debes proporcionar el correo del usuario.");
		}

		Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));

		if (usuario.getIntentosFallidos() < 5
				|| !"Bloqueo automático por intentos fallidos".equals(usuario.getMotivoBloqueo())) {

			throw new ConflictoNegocioException("La cuenta no tiene un bloqueo automático pendiente de reactivación.");
		}

		usuario.setIntentosFallidos(0);
		usuario.setMotivoBloqueo(null);
		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);
		registrarAuditoria(actorActual(), usuario.getIdUsuario(), "REACTIVAR_BLOQUEO_USUARIO", OffsetDateTime.now());
	}

	@Transactional
	public void restringirUsuario(UUID idUsuario, RestringirUsuarioRequest solicitud) {

		if (idUsuario == null || solicitud == null || solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()
				|| solicitud.getMotivo().trim().length() > 255) {

			throw new DatosInvalidosException("Debes proporcionar un motivo válido para la restricción.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new DatosInvalidosException("Usuario no encontrado."));

		if (usuario.isRestringido()) {
			throw new ConflictoNegocioException("La cuenta ya tiene una restricción administrativa.");
		}

		OffsetDateTime ahora = OffsetDateTime.now();

		usuario.setRestringido(true);
		usuario.setMotivoRestriccion(solicitud.getMotivo().trim());
		usuario.setFechaActualizacion(ahora);

		usuarioRepository.save(usuario);

		jwtUtil.invalidarSesiones(idUsuario);

		eventPublisher.publishEvent(new CuentaRestringidaEvent(idUsuario, ahora));
		registrarAuditoria(actorActual(), idUsuario, "RESTRINGIR_USUARIO", ahora);
	}

	@Transactional
	public void levantarRestriccion(UUID idUsuario) {

		if (idUsuario == null) {
			throw new DatosInvalidosException("Debes proporcionar el identificador del usuario.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new DatosInvalidosException("Usuario no encontrado."));

		if (!usuario.isRestringido()) {
			throw new ConflictoNegocioException("La cuenta no tiene una restricción administrativa vigente.");
		}

		usuario.setRestringido(false);
		usuario.setMotivoRestriccion(null);
		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);
		registrarAuditoria(actorActual(), idUsuario, "LEVANTAR_RESTRICCION_USUARIO", OffsetDateTime.now());
	}

	private UUID actorActual() {
		Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
		if (autenticacion != null && autenticacion.getPrincipal() instanceof Usuario actor) {
			return actor.getIdUsuario();
		}
		return null;
	}

	private void registrarAuditoria(UUID actor, UUID usuarioAfectado, String accion, OffsetDateTime fecha) {
		registrarAuditoria(actor, usuarioAfectado, accion, fecha, "{\"resultado\":\"COMPLETADO\"}");
	}

	private void registrarAuditoria(UUID actor, UUID usuarioAfectado, String accion, OffsetDateTime fecha,
			String detalle) {
		if (auditoria != null) {
			auditoria.registrarEvento(new RegistrarEventoAuditoria(
					actor, fecha, accion, "EXITOSO", "USUARIO", usuarioAfectado, detalle, null));
		}
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
