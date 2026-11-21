package co.edu.unbosque.marketplace.reserva.api;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.reserva.application.ReservaSolicitudService;
import co.edu.unbosque.marketplace.reserva.application.dto.CrearReservaRequest;
import co.edu.unbosque.marketplace.reserva.application.dto.SolicitudReservaResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

    private final ReservaSolicitudService reservaService;

    public ReservaController(ReservaSolicitudService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    public ResponseEntity<SolicitudReservaResponse> solicitar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idHuesped,
            @RequestHeader("Idempotency-Key") String claveIdempotencia,
            @Valid @RequestBody CrearReservaRequest solicitud) {

        SolicitudReservaResponse respuesta = reservaService.solicitar(
                idHuesped,
                solicitud,
                claveIdempotencia);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}