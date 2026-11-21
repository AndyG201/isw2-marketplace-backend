package co.edu.unbosque.marketplace.reserva.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.reserva.application.ReservaAnfitrionService;
import co.edu.unbosque.marketplace.reserva.application.dto.ReservaAlojamientoResponse;
import co.edu.unbosque.marketplace.reserva.application.dto.ReservasAlojamientoResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas/mis-alojamientos")
@SecurityRequirement(name = "bearerAuth")
public class ReservaAnfitrionController {

    private final ReservaAnfitrionService servicio;

    public ReservaAnfitrionController(ReservaAnfitrionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<ReservasAlojamientoResponse> consultarReservas(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            Authentication autenticacion) {
        validarAnfitrion(autenticacion);
        return ResponseEntity.ok(servicio.consultarReservas(idAnfitrion));
    }

    @GetMapping("/{idReserva}")
    public ResponseEntity<ReservaAlojamientoResponse> consultarReserva(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            Authentication autenticacion,
            @PathVariable UUID idReserva) {
        validarAnfitrion(autenticacion);
        return ResponseEntity.ok(servicio.consultarReserva(idAnfitrion, idReserva));
    }

    private void validarAnfitrion(Authentication autenticacion) {
        boolean esAnfitrion = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_ANFITRION".equals(autoridad.getAuthority()));

        if (!esAnfitrion) {
            throw new AccessDeniedException("Solo un anfitrión puede consultar las reservas de sus alojamientos.");
        }
    }
}
