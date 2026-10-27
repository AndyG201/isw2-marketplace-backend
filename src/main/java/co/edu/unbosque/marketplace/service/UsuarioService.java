
package co.edu.unbosque.marketplace.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.model.Usuario;
import co.edu.unbosque.marketplace.model.emun.EstadoUsuario;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.request.RegistrarUsuarioRequest;
import co.edu.unbosque.marketplace.security.ProteccionDocumento;
import co.edu.unbosque.marketplace.security.ProteccionContrasena;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

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

	public int registrar(RegistrarUsuarioRequest nuevoUsuario) {

		if (datosInvalidos(nuevoUsuario)) {
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
		Usuario usuario = crearUsuario(nuevoUsuario, datosDocumento);
		completarCampos(usuario);

		usuarioRepository.save(usuario);

		verificacionCorreoService.enviarCodigo(usuario.getCorreo());

		return 1;
	}

	private boolean datosInvalidos(RegistrarUsuarioRequest datos) {

		return datos == null || textoVacio(datos.getNombre()) || textoVacio(datos.getApellido())
				|| textoVacio(datos.getCorreo()) || textoVacio(datos.getTipoDocumento())
				|| textoVacio(datos.getDocumentoIdentidad()) || textoVacio(datos.getTelefono())
				|| textoVacio(datos.getContrasena()) || datos.getIdRol() == null;
	}

	private boolean textoVacio(String texto) {
		return texto == null || texto.isBlank();
	}

	private boolean correoExiste(String correo) {

		Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);
		return usuario.isPresent();
	}

	private boolean documentoExiste(byte[] documentoHash) {

		return usuarioRepository.existsByDocumentoHash(documentoHash);
	}

	private boolean telefonoExiste(String telefono) {

		return usuarioRepository.existsByTelefono(telefono);
	}

	private Usuario crearUsuario(RegistrarUsuarioRequest datos, ProteccionDocumento.DatosDocumento datosDocumento) {

		Usuario usuario = new Usuario();

		usuario.setNombre(datos.getNombre());
		usuario.setApellido(datos.getApellido());
		usuario.setCorreo(datos.getCorreo());
		usuario.setTipoDocumento(datos.getTipoDocumento());
		usuario.setTelefono(datos.getTelefono());
		usuario.setIdRol(datos.getIdRol());

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

		String clave = "recuperacion:contrasena:" + correoUsuario.toLowerCase(java.util.Locale.ROOT);

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

		String correoNormalizado = correo.trim().toLowerCase(java.util.Locale.ROOT);

		String claveCodigo = "recuperacion:contrasena:" + correoNormalizado;
		String claveIntentos = "recuperacion:contrasena:intentos:" + correoNormalizado;

		String hashGuardado = redisTemplate.opsForValue().get(claveCodigo);

		// Si no existe, el código expiró o no se solicitó.
		if (hashGuardado == null) {
			return false;
		}

		String hashIngresado = calcularHashCodigo(codigo);

		// El código es correcto.
		if (hashGuardado.equals(hashIngresado)) {
			return true;
		}

		// Aumentar el contador de intentos incorrectos.
		Long intentos = redisTemplate.opsForValue().increment(claveIntentos);

		// El contador también debe expirar en cinco minutos.
		if (intentos != null && intentos == 1) {
			redisTemplate.expire(claveIntentos, 5, TimeUnit.MINUTES);
		}

		// Al quinto intento incorrecto, invalidar el código.
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

		// Primero comprobamos que el código sea válido.
		if (!validarCodigoRecuperacion(correo, codigo)) {
			return false;
		}

		Optional<Usuario> resultado = usuarioRepository.findByCorreoIgnoreCase(correo.trim());

		if (resultado.isEmpty()) {
			return false;
		}

		Usuario usuario = resultado.get();

		// Guardamos la contraseña usando el hash seguro existente.
		usuario.setContrasenaHash(proteccionContrasena.generarHash(nuevaContrasena));

		usuario.setFechaActualizacion(OffsetDateTime.now());

		usuarioRepository.save(usuario);

		// Invalidamos el código y el contador para impedir su reutilización.
		String correoNormalizado = usuario.getCorreo().toLowerCase(java.util.Locale.ROOT);

		redisTemplate.delete("recuperacion:contrasena:" + correoNormalizado);

		redisTemplate.delete("recuperacion:contrasena:intentos:" + correoNormalizado);

		return true;
	}

}
