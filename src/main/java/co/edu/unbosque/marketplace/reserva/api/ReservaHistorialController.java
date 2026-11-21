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

import co.edu.unbosque.marketplace.reserva.application.ReservaHistorialService;
import co.edu.unbosque.marketplace.reserva.application.dto.MisReservasResponse;
import co.edu.unbosque.marketplace.reserva.application.dto.ReservaPropiaResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas/mis-reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaHistorialController {

    private final ReservaHistorialService servicio;

    public ReservaHistorialController(ReservaHistorialService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<MisReservasResponse> consultarMisReservas(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idHuesped,
            Authentication autenticacion) {
        validarHuesped(autenticacion);
        return ResponseEntity.ok(servicio.consultarMisReservas(idHuesped));
    }

    @GetMapping("/{idReserva}")
    public ResponseEntity<ReservaPropiaResponse> consultarMiReserva(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idHuesped,
            Authentication autenticacion,
            @PathVariable UUID idReserva) {
        validarHuesped(autenticacion);
        return ResponseEntity.ok(servicio.consultarMiReserva(idHuesped, idReserva));
    }

    private void validarHuesped(Authentication autenticacion) {
        boolean esHuesped = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_HUESPED".equals(autoridad.getAuthority()));

        if (!esHuesped) {
            throw new AccessDeniedException(
                    "Solo un huésped puede consultar su historial de reservas.");
        }
    }
}