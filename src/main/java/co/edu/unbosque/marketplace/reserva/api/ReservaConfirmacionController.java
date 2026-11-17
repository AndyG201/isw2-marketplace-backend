package co.edu.unbosque.marketplace.reserva.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.reserva.api.response.ConfirmacionReservaResponse;
import co.edu.unbosque.marketplace.reserva.application.ReservaConfirmacionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaConfirmacionController {
    private final ReservaConfirmacionService servicio;

    public ReservaConfirmacionController(ReservaConfirmacionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{idReserva}/retencion")
    public ResponseEntity<ConfirmacionReservaResponse> iniciarRetencion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idUsuario,
            @PathVariable UUID idReserva) {
        return ResponseEntity.ok(servicio.iniciarRetencion(idUsuario, idReserva));
    }

    @PostMapping("/{idReserva}/confirmacion-inmediata")
    public ResponseEntity<ConfirmacionReservaResponse> confirmar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idUsuario,
            @PathVariable UUID idReserva,
            @RequestHeader("Idempotency-Key") String claveIdempotencia) {
        return ResponseEntity.ok(servicio.confirmar(idUsuario, idReserva, claveIdempotencia));
    }
}
