package co.edu.unbosque.marketplace.usuario.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import co.edu.unbosque.marketplace.usuario.api.request.ActualizarContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.api.request.RegistrarUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.email.ExternalHTTPRequestMapping;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.RolRepository;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.security.ProteccionContrasena;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;
import co.edu.unbosque.marketplace.usuario.api.response.ConsultarCuentaResponse;
import co.edu.unbosque.marketplace.usuario.api.request.ActualizarPerfilRequest;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private RolRepository rolRepository;

	@Autowired
	private ProteccionDocumento proteccionDocumento;

	@Autowired
	private ProteccionContrasena proteccionContrasena;

	@Autowired
	private VerificacionCorreoService verificacionCorreoService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ExternalHTTPRequestMapping correoExterno;

	private final SecureRandom random = new SecureRandom();
	private static final long VIGENCIA_CAMBIO_CORREO = 5;
	private static final int MAX_INTENTOS_CAMBIO_CORREO = 5;

	public int registrar(RegistrarUsuarioRequest nuevoUsuario) {

		if (datosInvalidos(nuevoUsuario)) {
			return 0;
		}

		Optional<Rol> rolEncontrado = buscarRolPermitido(nuevoUsuario.getRol());

		if (rolEncontrado.isEmpty()) {
			return 0;
		}

		if (correoExiste(nuevoUsuario.getCorreo())) {
			return 0;
		}

		if (telefonoExiste(nuevoUsuario.getTelefono())) {
			return 0;
		}

		ProteccionDocumento.DatosDocumento datosDocumento = proteccionDocumento
				.protegerDocumento(nuevoUsuario.getTipoDocumento(), nuevoUsuario.getDocumentoIdentidad());

		if (documentoExiste(datosDocumento.documentoHash())) {
			return 0;
		}

		Usuario usuario = crearUsuario(nuevoUsuario, datosDocumento, rolEncontrado.get());

		completarCampos(usuario);

		usuarioRepository.save(usuario);

		verificacionCorreoService.enviarCodigo(usuario.getCorreo());

		return 1;
	}

	private Optional<Rol> buscarRolPermitido(String nombreRol) {

		if (textoVacio(nombreRol)) {
			return Optional.empty();
		}

		String rolNormalizado = nombreRol.trim().toUpperCase(Locale.ROOT);

		if (!rolNormalizado.equals("HUESPED") && !rolNormalizado.equals("ANFITRION")) {
			return Optional.empty();
		}

		return rolRepository.findByNombreIgnoreCase(rolNormalizado);
	}

	private boolean datosInvalidos(RegistrarUsuarioRequest datos) {

		return datos == null || textoVacio(datos.getNombre()) || textoVacio(datos.getApellido())
				|| textoVacio(datos.getCorreo()) || textoVacio(datos.getTipoDocumento())
				|| textoVacio(datos.getDocumentoIdentidad()) || textoVacio(datos.getTelefono())
				|| textoVacio(datos.getContrasena()) || textoVacio(datos.getRol());
	}

	private boolean textoVacio(String texto) {
		return texto == null || texto.isBlank();
	}

	private boolean correoExiste(String correo) {

		return usuarioRepository.findByCorreoIgnoreCase(correo.trim()).isPresent();
	}

	private boolean documentoExiste(byte[] documentoHash) {

		return usuarioRepository.existsByDocumentoHash(documentoHash);
	}

	private boolean telefonoExiste(String telefono) {

		return usuarioRepository.existsByTelefono(telefono.trim());
	}

	private Usuario crearUsuario(RegistrarUsuarioRequest datos, ProteccionDocumento.DatosDocumento datosDocumento,
			Rol rol) {

		Usuario usuario = new Usuario();

		usuario.setNombre(datos.getNombre());
		usuario.setApellido(datos.getApellido());
		usuario.setCorreo(datos.getCorreo().trim());
		usuario.setTipoDocumento(datos.getTipoDocumento());
		usuario.setTelefono(datos.getTelefono().trim());

		usuario.setRol(rol);

		usuario.setDocumentoCifrado(datosDocumento.documentoCifrado());
		usuario.setDocumentoNonce(datosDocumento.documentoNonce());
		usuario.setDocumentoHash(datosDocumento.documentoHash());

		usuario.setContrasenaHash(proteccionContrasena.generarHash(datos.getContrasena()));

		return usuario;
	}

	private void completarCampos(Usuario usuario) {

		OffsetDateTime ahora = OffsetDateTime.now();

		usuario.setIdUsuario(UUID.randomUUID());
		usuario.setCorreoVerificado(false);
		usuario.setEstado(EstadoUsuario.PENDIENTE_VERIFICACION);
		usuario.setRestringido(false);
		usuario.setIntentosFallidos(0);
		usuario.setMotivoRestriccion(null);
		usuario.setFechaRegistro(ahora);
		usuario.setFechaActualizacion(ahora);
	}

	public boolean generarCodigoRecuperacion(String correo) {

		if (correo == null || correo.isBlank()) {
			return false;
		}

		Optional<Usuario> resultado = usuarioRepository.findByCorreoIgnoreCase(correo.trim());

		if (resultado.isEmpty()) {
			return false;
		}

		String correoUsuario = resultado.get().getCorreo();

		String codigo = String.format("%06d", random.nextInt(1_000_000));

		String clave = "recuperacion:contrasena:" + correoUsuario.toLowerCase(Locale.ROOT);

		redisTemplate.opsForValue().set(clave, calcularHashCodigo(codigo), 5, TimeUnit.MINUTES);

		try {
			correoExterno.enviarCodigoRecuperacion(correoUsuario, codigo);
			return true;

		} catch (Exception e) {
			redisTemplate.delete(clave);
			throw e;
		}
	}

	private String calcularHashCodigo(String codigo) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(codigo.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(hash);

		} catch (Exception e) {
			throw new IllegalStateException("No se pudo generar el hash del código", e);
		}
	}

	public boolean validarCodigoRecuperacion(String correo, String codigo) {

		if (correo == null || correo.isBlank() || codigo == null || !codigo.matches("\\d{6}")) {
			return false;
		}

		String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);

		String claveCodigo = "recuperacion:contrasena:" + correoNormalizado;

		String claveIntentos = "recuperacion:contrasena:intentos:" + correoNormalizado;

		String hashGuardado = redisTemplate.opsForValue().get(claveCodigo);

		if (hashGuardado == null) {
			return false;
		}

		String hashIngresado = calcularHashCodigo(codigo);

		if (hashGuardado.equals(hashIngresado)) {
			return true;
		}

		Long intentos = redisTemplate.opsForValue().increment(claveIntentos);

		if (intentos != null && intentos == 1) {
			redisTemplate.expire(claveIntentos, 5, TimeUnit.MINUTES);
		}

		if (intentos != null && intentos >= 5) {
			redisTemplate.delete(claveCodigo);
			redisTemplate.delete(claveIntentos);
		}

		return false;
	}

	public boolean restablecerContrasena(String correo, String codigo, String nuevaContrasena) {

		if (correo == null || correo.isBlank() || nuevaContrasena == null || nuevaContrasena.isBlank()) {
			return false;
		}

		if (!validarCodigoRecuperacion(correo, codigo)) {
			return false;
		}

		Optional<Usuario> resultado = usuarioRepository.findByCorreoIgnoreCase(correo.trim());

		if (resultado.isEmpty()) {
			return false;
		}

		Usuario usuario = resultado.get();

		usuario.setContrasenaHash(proteccionContrasena.generarHash(nuevaContrasena));

		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);

		String correoNormalizado = usuario.getCorreo().toLowerCase(Locale.ROOT);

		redisTemplate.delete("recuperacion:contrasena:" + correoNormalizado);

		redisTemplate.delete("recuperacion:contrasena:intentos:" + correoNormalizado);

		return true;
	}

	public void reactivarBloqueo(String correo) {

		if (correo == null || correo.isBlank()) {
			throw new IllegalArgumentException("Debes proporcionar el correo del usuario");
		}

		Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		if (usuario.getIntentosFallidos() < 5
				|| !"Bloqueo automático por intentos fallidos".equals(usuario.getMotivoBloqueo())) {

			throw new IllegalStateException("La cuenta no tiene un bloqueo automático pendiente de reactivación");
		}

		usuario.setIntentosFallidos(0);
		usuario.setMotivoBloqueo(null);
		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);
	}

	public ConsultarCuentaResponse consultarMiCuenta(UUID idUsuario) {

		if (idUsuario == null) {
			throw new IllegalArgumentException("No se pudo identificar al usuario.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		if (usuario.getRol() == null) {
			throw new IllegalStateException("El usuario no tiene un rol asignado.");
		}

		String documento = proteccionDocumento.descifrarDocumento(usuario.getDocumentoCifrado(),
				usuario.getDocumentoNonce());

		return new ConsultarCuentaResponse(usuario.getNombre(), usuario.getApellido(), usuario.getCorreo(),
				usuario.getTipoDocumento(), documento, usuario.getRol().getNombre());
	}

	public String actualizarPerfil(UUID idUsuario, ActualizarPerfilRequest solicitud) {

		if (idUsuario == null || solicitud == null || solicitud.getNombre() == null || solicitud.getNombre().isBlank()
				|| solicitud.getApellido() == null || solicitud.getApellido().isBlank() || solicitud.getCorreo() == null
				|| solicitud.getCorreo().isBlank()) {

			throw new IllegalArgumentException("Debes proporcionar todos los datos del perfil.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		String nombre = solicitud.getNombre().trim();
		String apellido = solicitud.getApellido().trim();
		String correoNuevo = solicitud.getCorreo().trim();

		boolean cambioCorreo = !usuario.getCorreo().equalsIgnoreCase(correoNuevo);

		if (cambioCorreo) {

			if (usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(correoNuevo, idUsuario)) {

				throw new IllegalStateException("El correo electrónico ya está asociado a otra cuenta.");
			}

			String codigo = String.format("%06d", random.nextInt(1_000_000));

			String prefijo = "perfil:cambio-correo:" + idUsuario;

			redisTemplate.opsForValue().set(prefijo + ":correo", correoNuevo, VIGENCIA_CAMBIO_CORREO, TimeUnit.MINUTES);

			redisTemplate.opsForValue().set(prefijo + ":codigo", calcularHashCodigo(codigo), VIGENCIA_CAMBIO_CORREO,
					TimeUnit.MINUTES);

			redisTemplate.opsForValue().set(prefijo + ":intentos", "0", VIGENCIA_CAMBIO_CORREO, TimeUnit.MINUTES);

			try {

				correoExterno.enviarCodigoVerificacion(correoNuevo, codigo);

			} catch (RuntimeException e) {

				eliminarDatosCambioCorreo(idUsuario);

				throw new IllegalStateException("No se pudo enviar el código al nuevo correo.", e);
			}
		}

		usuario.setNombre(nombre);
		usuario.setApellido(apellido);
		usuario.setFechaActualizacion(OffsetDateTime.now());

		try {
			usuarioRepository.save(usuario);

		} catch (RuntimeException e) {

			if (cambioCorreo) {
				eliminarDatosCambioCorreo(idUsuario);
			}

			throw e;
		}

		if (cambioCorreo) {
			return "Perfil actualizado. Se envió un código al nuevo correo para confirmar el cambio.";
		}

		return "Perfil actualizado correctamente.";
	}

	public void confirmarCambioCorreo(UUID idUsuario, String codigo) {

		if (idUsuario == null || codigo == null || !codigo.matches("\\d{6}")) {

			throw new IllegalArgumentException("Debes proporcionar un código válido de seis dígitos.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		String prefijo = "perfil:cambio-correo:" + idUsuario;

		String correoNuevo = redisTemplate.opsForValue().get(prefijo + ":correo");

		String hashGuardado = redisTemplate.opsForValue().get(prefijo + ":codigo");

		if (correoNuevo == null || hashGuardado == null) {

			eliminarDatosCambioCorreo(idUsuario);

			throw new IllegalStateException("El código ha expirado o no existe una solicitud pendiente.");
		}

		String hashIngresado = calcularHashCodigo(codigo);

		if (!hashGuardado.equals(hashIngresado)) {

			Long intentos = redisTemplate.opsForValue().increment(prefijo + ":intentos");

			if (intentos != null && intentos >= MAX_INTENTOS_CAMBIO_CORREO) {

				eliminarDatosCambioCorreo(idUsuario);

				throw new IllegalStateException(
						"Superaste los cinco intentos. Solicita nuevamente el cambio de correo.");
			}

			throw new IllegalArgumentException("El código de verificación es incorrecto.");
		}

		if (usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot(correoNuevo, idUsuario)) {

			eliminarDatosCambioCorreo(idUsuario);

			throw new IllegalStateException("El correo electrónico ya está asociado a otra cuenta.");
		}

		usuario.setCorreo(correoNuevo);
		usuario.setCorreoVerificado(true);
		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);

		eliminarDatosCambioCorreo(idUsuario);
	}

	private void eliminarDatosCambioCorreo(UUID idUsuario) {

		String prefijo = "perfil:cambio-correo:" + idUsuario;

		redisTemplate.delete(prefijo + ":correo");
		redisTemplate.delete(prefijo + ":codigo");
		redisTemplate.delete(prefijo + ":intentos");
	}

	public void actualizarContrasena(UUID idUsuario, ActualizarContrasenaRequest solicitud) {

		if (idUsuario == null || solicitud == null || textoVacio(solicitud.getContrasenaActual())
				|| textoVacio(solicitud.getNuevaContrasena())) {

			throw new IllegalArgumentException("Debes proporcionar la contraseña actual y la nueva contraseña.");
		}

		Usuario usuario = usuarioRepository.findById(idUsuario)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

		if (!proteccionContrasena.verificar(solicitud.getContrasenaActual(), usuario.getContrasenaHash())) {

			throw new IllegalArgumentException("La contraseña actual es incorrecta.");
		}

		usuario.setContrasenaHash(proteccionContrasena.generarHash(solicitud.getNuevaContrasena()));

		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);
	}

}