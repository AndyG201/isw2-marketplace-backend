package co.edu.unbosque.marketplace.disponibilidad.api;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.disponibilidad.application.CalendarioDisponibilidadService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/alojamientos")
public class CalendarioDisponibilidadController {

    private final CalendarioDisponibilidadService calendarioService;

    public CalendarioDisponibilidadController(
            CalendarioDisponibilidadService calendarioService) {

        this.calendarioService = calendarioService;
    }

    @GetMapping("/{idAlojamiento}/disponibilidad")
    public ResponseEntity<?> consultarPorFechas(
            @PathVariable UUID idAlojamiento,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        try {

            return ResponseEntity.ok(
                    calendarioService.consultarPorFechas(
                            idAlojamiento,
                            fechaInicio,
                            fechaFin
                    )
            );

        } catch (NoSuchElementException e) {

            return error(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalArgumentException e) {

            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{idAlojamiento}/calendario")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> consultarCalendarioCompleto(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idAlojamiento) {

        if (idAnfitrion == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para consultar el calendario completo."
            );
        }

        try {

            return ResponseEntity.ok(
                    calendarioService.consultarCalendarioCompleto(
                            idAnfitrion,
                            idAlojamiento
                    )
            );

        } catch (AccessDeniedException e) {

            return error(HttpStatus.FORBIDDEN, e.getMessage());

        } catch (NoSuchElementException e) {

            return error(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalArgumentException e) {

            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus estado,
            String mensaje) {

        return ResponseEntity.status(estado)
                .body(Map.of("mensaje", mensaje));
    }
}
