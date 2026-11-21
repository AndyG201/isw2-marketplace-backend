package co.edu.unbosque.marketplace.reserva.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.reserva.application.ReservaDecisionService;
import co.edu.unbosque.marketplace.reserva.application.dto.DecisionReservaResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaDecisionController {

    private final ReservaDecisionService servicio;

    public ReservaDecisionController(ReservaDecisionService servicio) {
        this.servicio = servicio;
    }

    @PatchMapping("/{idReserva}/aprobar")
    public ResponseEntity<DecisionReservaResponse> aprobar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            Authentication autenticacion,
            @PathVariable UUID idReserva) {

        validarAnfitrion(autenticacion);
        return ResponseEntity.ok(servicio.aprobar(idAnfitrion, idReserva));
    }

    @PatchMapping("/{idReserva}/rechazar")
    public ResponseEntity<DecisionReservaResponse> rechazar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            Authentication autenticacion,
            @PathVariable UUID idReserva) {

        validarAnfitrion(autenticacion);
        return ResponseEntity.ok(servicio.rechazar(idAnfitrion, idReserva));
    }

    private void validarAnfitrion(Authentication autenticacion) {
        boolean esAnfitrion = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_ANFITRION".equals(autoridad.getAuthority()));

        if (!esAnfitrion) {
            throw new AccesoNoPermitidoException(
                    "Solo un anfitrión puede decidir sobre solicitudes de reserva.");
        }
    }
}