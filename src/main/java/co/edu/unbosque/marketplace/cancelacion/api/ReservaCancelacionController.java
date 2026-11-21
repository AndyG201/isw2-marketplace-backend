package co.edu.unbosque.marketplace.cancelacion.api;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.cancelacion.api.request.CancelarReservaRequest;
import co.edu.unbosque.marketplace.cancelacion.application.CancelacionReservaService;
import co.edu.unbosque.marketplace.cancelacion.application.dto.CancelarReservaResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaCancelacionController {

    private final CancelacionReservaService servicio;

    public ReservaCancelacionController(CancelacionReservaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{idReserva}/cancelacion")
    public ResponseEntity<CancelarReservaResponse> cancelar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idHuesped,
            Authentication autenticacion,
            @PathVariable UUID idReserva,
            @Valid @RequestBody CancelarReservaRequest solicitud) {
        validarHuesped(autenticacion);
        return ResponseEntity.ok(servicio.cancelar(idHuesped, idReserva, solicitud.motivo()));
    }

    private void validarHuesped(Authentication autenticacion) {
        boolean esHuesped = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_HUESPED".equals(autoridad.getAuthority()));
        if (!esHuesped) {
            throw new AccesoNoPermitidoException("Solo un huésped puede cancelar su reserva.");
        }
    }
}
