
package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import co.edu.unbosque.marketplace.usuario.application.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class AdministradorUsuarioController {

    private final UsuarioService usuarioService;

    public AdministradorUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PatchMapping("/reactivar-bloqueo")
    public ResponseEntity<String> reactivarBloqueo(
            @RequestParam("correo")
            @NotBlank(message = "Debes proporcionar un correo.")
            @Email(message = "El correo no tiene un formato válido.")
            @Size(max = 254, message = "El correo supera la longitud permitida.")
            String correo) {

        try {
            usuarioService.reactivarBloqueo(correo.trim());

            return ResponseEntity.ok(
                    "La cuenta ha sido reactivada correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}
