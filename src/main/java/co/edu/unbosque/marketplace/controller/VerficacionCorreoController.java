package co.edu.unbosque.marketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.service.VerificacionCorreoService;

@RestController
@RequestMapping("/verificacion-correo")
@CrossOrigin(origins = "*")
public class VerficacionCorreoController {

	@Autowired
	private VerificacionCorreoService verificacionCorreoService;

	@PostMapping("/confirmar")
	public ResponseEntity<String> confirmarCodigo(@RequestParam String correo, String codigo) {

		if (correo == null || correo.isBlank() || codigo == null || codigo.isBlank()) {
			return ResponseEntity.badRequest().body("Debes proporcionar el correo y el codigo");
		}

		try {
			verificacionCorreoService.confirmarCodigo(correo, codigo);

			return ResponseEntity.ok("Correo verificado correctamente");

		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}

	}

	@PostMapping("/reenviar")
	public ResponseEntity<String> reenviarCodigo(@RequestParam String correo) {

		if (correo == null || correo.isBlank()) {
			return ResponseEntity.badRequest().body("Debes proporcionar un correo");
		}

		try {
			verificacionCorreoService.enviarCodigo(correo);
			return ResponseEntity.ok("Se envio un nuevo codigo al correo");

		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

}
