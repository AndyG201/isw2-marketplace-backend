package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.usuario.application.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/usuarios")
public class AdministradorUsuarioController {

    private final UsuarioService usuarioService;

    public AdministradorUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/reactivar-bloqueo")
    public ResponseEntity<String> reactivarBloqueo(
            @RequestParam String correo) {

        try {
            usuarioService.reactivarBloqueo(correo);

            return ResponseEntity.ok(
                    "La cuenta ha sido reactivada correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}