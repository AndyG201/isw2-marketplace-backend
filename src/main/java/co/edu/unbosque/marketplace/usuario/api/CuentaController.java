
package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.usuario.api.request.ActualizarContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.api.request.ActualizarPerfilRequest;
import co.edu.unbosque.marketplace.usuario.api.request.ConfirmarCambioCorreoRequest;
import co.edu.unbosque.marketplace.usuario.api.response.ConsultarCuentaResponse;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class CuentaController {

	private final UsuarioService usuarioService;

	public CuentaController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/mi_cuenta")
	public ResponseEntity<?> consultarMiCuenta(@AuthenticationPrincipal Usuario usuario) {

		if (usuario == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		ConsultarCuentaResponse respuesta = usuarioService.consultarMiCuenta(usuario.getIdUsuario());

		return ResponseEntity.ok(respuesta);
	}

	@PutMapping("/mi_cuenta/perfil")
	public ResponseEntity<String> actualizarPerfil(@AuthenticationPrincipal Usuario usuario,
			@Valid @RequestBody ActualizarPerfilRequest solicitud) {

		if (usuario == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			String resultado = usuarioService.actualizarPerfil(usuario.getIdUsuario(), solicitud);

			return ResponseEntity.ok(resultado);

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());

		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PostMapping("/mi_cuenta/correo/confirmar")
	public ResponseEntity<String> confirmarCambioCorreo(@AuthenticationPrincipal Usuario usuario,
			@Valid @RequestBody ConfirmarCambioCorreoRequest solicitud) {

		if (usuario == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			usuarioService.confirmarCambioCorreo(usuario.getIdUsuario(), solicitud.getCodigo());

			return ResponseEntity.ok("El correo electrónico se actualizó y verificó correctamente.");

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());

		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

	@PatchMapping("/mi_cuenta/contrasena")
	public ResponseEntity<String> actualizarContrasena(@AuthenticationPrincipal Usuario usuario,
			@Valid @RequestBody ActualizarContrasenaRequest solicitud) {

		if (usuario == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			usuarioService.actualizarContrasena(usuario.getIdUsuario(), solicitud);

			return ResponseEntity.ok("La contraseña se actualizó correctamente.");

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PatchMapping("/mi_cuenta/desactivar")
	public ResponseEntity<String> desactivarCuenta(@AuthenticationPrincipal Usuario usuario) {

		if (usuario == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			usuarioService.desactivarCuenta(usuario.getIdUsuario());

			return ResponseEntity.ok("Tu cuenta ha sido desactivada correctamente.");

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());

		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		}
	}

}
