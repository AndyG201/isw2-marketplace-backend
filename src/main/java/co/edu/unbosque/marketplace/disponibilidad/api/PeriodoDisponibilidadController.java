package co.edu.unbosque.marketplace.disponibilidad.api;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.disponibilidad.api.request.PeriodoDisponibilidadRequest;
import co.edu.unbosque.marketplace.disponibilidad.application.PeriodoDisponibilidadService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/alojamientos/{idAlojamiento}/periodos-disponibles")
@SecurityRequirement(name = "bearerAuth")
public class PeriodoDisponibilidadController {

    private final PeriodoDisponibilidadService disponibilidadService;

    public PeriodoDisponibilidadController(
            PeriodoDisponibilidadService disponibilidadService) {

        this.disponibilidadService = disponibilidadService;
    }

    @PostMapping
    public ResponseEntity<?> registrarPeriodo(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idAlojamiento,
            @Valid @RequestBody PeriodoDisponibilidadRequest solicitud,
            BindingResult resultadoValidacion) {

        if (idAnfitrion == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para registrar periodos."
            );
        }

        if (resultadoValidacion.hasErrors()) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    obtenerErrores(resultadoValidacion)
            );
        }

        try {

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(disponibilidadService.registrarPeriodo(
                            idAnfitrion,
                            idAlojamiento,
                            solicitud
                    ));

        } catch (AccessDeniedException e) {

            return error(HttpStatus.FORBIDDEN, e.getMessage());

        } catch (NoSuchElementException e) {

            return error(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalArgumentException e) {

            return error(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IllegalStateException e) {

            return error(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> consultarPeriodos(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idAlojamiento) {

        if (idAnfitrion == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para consultar los periodos."
            );
        }

        try {

            return ResponseEntity.ok(
                    disponibilidadService.consultarPeriodos(
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

    @PutMapping("/{idPeriodo}")
    public ResponseEntity<?> actualizarPeriodo(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idAlojamiento,
            @PathVariable UUID idPeriodo,
            @Valid @RequestBody PeriodoDisponibilidadRequest solicitud,
            BindingResult resultadoValidacion) {

        if (idAnfitrion == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para actualizar periodos."
            );
        }

        if (resultadoValidacion.hasErrors()) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    obtenerErrores(resultadoValidacion)
            );
        }

        try {

            return ResponseEntity.ok(
                    disponibilidadService.actualizarPeriodo(
                            idAnfitrion,
                            idAlojamiento,
                            idPeriodo,
                            solicitud
                    )
            );

        } catch (AccessDeniedException e) {

            return error(HttpStatus.FORBIDDEN, e.getMessage());

        } catch (NoSuchElementException e) {

            return error(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalArgumentException e) {

            return error(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IllegalStateException e) {

            return error(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping("/{idPeriodo}")
    public ResponseEntity<?> deshabilitarPeriodo(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idAlojamiento,
            @PathVariable UUID idPeriodo) {

        if (idAnfitrion == null) {
            return error(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para deshabilitar periodos."
            );
        }

        try {

            return ResponseEntity.ok(
                    disponibilidadService.deshabilitarPeriodo(
                            idAnfitrion,
                            idAlojamiento,
                            idPeriodo
                    )
            );

        } catch (AccessDeniedException e) {

            return error(HttpStatus.FORBIDDEN, e.getMessage());

        } catch (NoSuchElementException e) {

            return error(HttpStatus.NOT_FOUND, e.getMessage());

        } catch (IllegalArgumentException e) {

            return error(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IllegalStateException e) {

            return error(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    private String obtenerErrores(BindingResult resultado) {

        return resultado.getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(" "));
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus estado,
            String mensaje) {

        return ResponseEntity.status(estado)
                .body(Map.of("mensaje", mensaje));
    }
}
