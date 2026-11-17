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
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoAprobadoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaPagoAprobadoController {

    private final ReservaPagoAprobadoService servicio;

    public ReservaPagoAprobadoController(ReservaPagoAprobadoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/{idReserva}/pago-aprobado")
    public ResponseEntity<ConfirmacionReservaResponse> pagar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idHuesped,
            @PathVariable UUID idReserva,
            @RequestHeader("Idempotency-Key") String claveIdempotencia) {
        return ResponseEntity.ok(servicio.pagar(idHuesped, idReserva, claveIdempotencia));
    }
}