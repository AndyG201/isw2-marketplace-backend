package co.edu.unbosque.marketplace.resena.api;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.resena.api.request.OcultarResenaRequest;
import co.edu.unbosque.marketplace.resena.api.response.OcultarResenaResponse;
import co.edu.unbosque.marketplace.resena.application.ResenaModeracionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/resenas")
@SecurityRequirement(name = "bearerAuth")
public class ResenaModeracionController {

    private final ResenaModeracionService servicio;

    public ResenaModeracionController(ResenaModeracionService servicio) {
        this.servicio = servicio;
    }

    @PatchMapping("/{idResena}/ocultar")
    public ResponseEntity<OcultarResenaResponse> ocultar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAdministrador,
            Authentication autenticacion,
            @PathVariable UUID idResena,
            @Valid @RequestBody OcultarResenaRequest solicitud) {
        validarAdministrador(idAdministrador, autenticacion);
        return ResponseEntity.ok(servicio.ocultar(idAdministrador, idResena, solicitud.motivo()));
    }

    private void validarAdministrador(UUID idAdministrador, Authentication autenticacion) {
        if (idAdministrador == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión como administrador.");
        }
        boolean esAdministrador = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_ADMINISTRADOR".equals(autoridad.getAuthority()));
        if (!esAdministrador) {
            throw new AccessDeniedException("Solo un administrador puede ocultar una reseña.");
        }
    }
}
