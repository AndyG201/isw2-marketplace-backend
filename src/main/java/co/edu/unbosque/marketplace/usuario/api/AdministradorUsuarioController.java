
package co.edu.unbosque.marketplace.usuario.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import co.edu.unbosque.marketplace.usuario.api.request.RestringirUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.api.response.AdministradorUsuarioResponse;
import co.edu.unbosque.marketplace.usuario.application.AdministradorService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class AdministradorUsuarioController {

    private final AdministradorService administradorService;

    public AdministradorUsuarioController(
            AdministradorService administradorService) {

        this.administradorService = administradorService;
    }

    @PatchMapping("/reactivar-bloqueo")
    public ResponseEntity<String> reactivarBloqueo(
            @RequestParam("correo")
            @NotBlank(message = "Debes proporcionar un correo.")
            @Email(message = "El correo no tiene un formato válido.")
            @Size(max = 254, message = "El correo supera la longitud permitida.")
            String correo) {

        try {
            administradorService.reactivarBloqueo(correo.trim());

            return ResponseEntity.ok(
                    "La cuenta ha sido reactivada correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @PatchMapping("/{idUsuario}/restringir")
    public ResponseEntity<String> restringirUsuario(
            @PathVariable UUID idUsuario,
            @Valid @RequestBody RestringirUsuarioRequest solicitud) {

        try {
            administradorService.restringirUsuario(
                    idUsuario,
                    solicitud
            );

            return ResponseEntity.ok(
                    "La cuenta ha sido restringida correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @PatchMapping("/{idUsuario}/levantar-restriccion")
    public ResponseEntity<String> levantarRestriccion(
            @PathVariable UUID idUsuario) {

        try {
            administradorService.levantarRestriccion(idUsuario);

            return ResponseEntity.ok(
                    "La restricción administrativa ha sido levantada correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AdministradorUsuarioResponse>> consultarUsuarios() {

        List<AdministradorUsuarioResponse> usuarios =
                administradorService.consultarUsuarios();

        return ResponseEntity.ok(usuarios);
    }
}
