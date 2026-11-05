
package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> consultarMiCuenta(
            @AuthenticationPrincipal Usuario usuario) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ConsultarCuentaResponse respuesta =
                usuarioService.consultarMiCuenta(usuario.getIdUsuario());

        return ResponseEntity.ok(respuesta);
    }
}
