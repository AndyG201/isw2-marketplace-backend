package co.edu.unbosque.marketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.dto.RegistrarUsuarioDTO;
import co.edu.unbosque.marketplace.service.UsuarioService;

@RestController
@RequestMapping("/autenticacion")
@CrossOrigin(origins = "*")
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;
	
	@PostMapping("/registrar")
	public ResponseEntity<String> registrar (@RequestBody RegistrarUsuarioDTO nuevoUsuario){
		int resultado = usuarioService.registrar(nuevoUsuario);
		if(resultado == 1) {
			return ResponseEntity.ok("Usuario creado");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Datos invalidos");
	}
}
