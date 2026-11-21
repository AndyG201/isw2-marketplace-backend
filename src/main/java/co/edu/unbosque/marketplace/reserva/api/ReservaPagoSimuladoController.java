package co.edu.unbosque.marketplace.reserva.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.reserva.application.ReservaPagoSimuladoService;
import co.edu.unbosque.marketplace.reserva.application.dto.PagoSimuladoResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaPagoSimuladoController {

    private final ReservaPagoSimuladoService servicio;

    public ReservaPagoSimuladoController(ReservaPagoSimuladoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{idReserva}/pago-simulado")
    public ResponseEntity<PagoSimuladoResponse> pagar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idHuesped,
            Authentication autenticacion,
            @PathVariable UUID idReserva,
            @RequestHeader("Idempotency-Key") String claveIdempotencia) {
        validarHuesped(autenticacion);
        return ResponseEntity.ok(servicio.pagar(idHuesped, idReserva, claveIdempotencia));
    }

    private void validarHuesped(Authentication autenticacion) {
        boolean esHuesped = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_HUESPED".equals(autoridad.getAuthority()));
        if (!esHuesped) {
            throw new AccessDeniedException("Solo un huésped puede pagar una reserva.");
        }
    }
}
