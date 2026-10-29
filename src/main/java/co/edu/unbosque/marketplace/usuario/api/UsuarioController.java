
package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.usuario.api.request.AutenticacionRequest;
import co.edu.unbosque.marketplace.usuario.api.request.RegistrarUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.api.response.AutenticacionResponse;
import co.edu.unbosque.marketplace.usuario.application.AutenticacionService;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;

@RestController
@RequestMapping("/autenticacion")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AutenticacionService autenticacionService;

    public UsuarioController(
            UsuarioService usuarioService,
            AutenticacionService autenticacionService) {
        this.usuarioService = usuarioService;
        this.autenticacionService = autenticacionService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(
            @RequestBody RegistrarUsuarioRequest nuevoUsuario) {

        int resultado = usuarioService.registrar(nuevoUsuario);

        if (resultado == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario creado. Verifica tu correo para activar el acceso.");
        }

        return ResponseEntity.badRequest().body("Datos inválidos o usuario existente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(
            @RequestBody AutenticacionRequest solicitud) {

        try {
            String token = autenticacionService.iniciarSesion(solicitud);

            return ResponseEntity.ok(new AutenticacionResponse(token));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }
}
