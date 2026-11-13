
package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.publicacion.api.request.BloquearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.request.DesbloquearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.ModeracionPublicacionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionModeracionController {

    private final ModeracionPublicacionService moderacionService;

    public PublicacionModeracionController(
            ModeracionPublicacionService moderacionService) {

        this.moderacionService = moderacionService;
    }

    // CONSULTAR PUBLICACIONES

    @GetMapping
    public ResponseEntity<?> consultarPublicaciones() {

        return ResponseEntity.ok(
                moderacionService.consultarPublicaciones()
        );
    }

    // BLOQUEAR PUBLICACION

    @PatchMapping("/{idPublicacion}/bloquear")
    public ResponseEntity<?> bloquearPublicacion(
            @AuthenticationPrincipal(expression = "idUsuario")
            UUID idAdministrador,

            @PathVariable UUID idPublicacion,

            @Valid @RequestBody BloquearPublicacionRequest solicitud,

            BindingResult resultadoValidacion) {

        if (idAdministrador == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión como administrador."
                    ));
        }

        if (resultadoValidacion.hasErrors()) {

            String mensaje = obtenerErroresValidacion(
                    resultadoValidacion
            );

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", mensaje));
        }

        try {

            return ResponseEntity.ok(
                    moderacionService.bloquearPublicacion(
                            idAdministrador,
                            idPublicacion,
                            solicitud.getMotivo()
                    )
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // DESBLOQUEAR PUBLICACION

    @PatchMapping("/{idPublicacion}/desbloquear")
    public ResponseEntity<?> desbloquearPublicacion(
            @AuthenticationPrincipal(expression = "idUsuario")
            UUID idAdministrador,

            @PathVariable UUID idPublicacion,

            @Valid @RequestBody DesbloquearPublicacionRequest solicitud,

            BindingResult resultadoValidacion) {

        if (idAdministrador == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión como administrador."
                    ));
        }

        if (resultadoValidacion.hasErrors()) {

            String mensaje = obtenerErroresValidacion(
                    resultadoValidacion
            );

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", mensaje));
        }

        try {

            return ResponseEntity.ok(
                    moderacionService.desbloquearPublicacion(
                            idAdministrador,
                            idPublicacion,
                            solicitud.getMotivo()
                    )
            );

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // CONSTRUIR MENSAJES DE VALIDACION

    private String obtenerErroresValidacion(
            BindingResult resultadoValidacion) {

        return resultadoValidacion.getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(" "));
    }
}
