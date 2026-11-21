package co.edu.unbosque.marketplace.resena.api;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.resena.application.ResenaService;
import co.edu.unbosque.marketplace.resena.application.dto.CrearResenaRequest;
import co.edu.unbosque.marketplace.resena.application.dto.CrearResenaResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ResenaController {

    private final ResenaService servicio;

    public ResenaController(ResenaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{idReserva}/resenas")
    public ResponseEntity<CrearResenaResponse> crear(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idHuesped,
            Authentication autenticacion,
            @PathVariable UUID idReserva,
            @Valid @RequestBody CrearResenaRequest solicitud) {
        validarHuesped(autenticacion);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.crear(idHuesped, idReserva, solicitud));
    }

    private void validarHuesped(Authentication autenticacion) {
        boolean esHuesped = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_HUESPED".equals(autoridad.getAuthority()));
        if (!esHuesped) {
            throw new AccessDeniedException("Solo un huésped puede crear una reseña.");
        }
    }
}
