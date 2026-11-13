package co.edu.unbosque.marketplace.publicacion.api;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.publicacion.api.request.ConfigurarModoAceptacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.ModoAceptacionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionModoAceptacionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionModoAceptacionController {

    private final PublicacionModoAceptacionService modoAceptacionService;

    public PublicacionModoAceptacionController(
            PublicacionModoAceptacionService modoAceptacionService) {

        this.modoAceptacionService = modoAceptacionService;
    }

    @GetMapping("/{idPublicacion}/modo-aceptacion")
    public ResponseEntity<?> consultarModoAceptacion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idActor,
            @PathVariable UUID idPublicacion) {

        if (idActor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para consultar esta configuración."
                    ));
        }

        try {

            ModoAceptacionPublicacionResponse respuesta =
                    modoAceptacionService.consultarModoAceptacion(
                            idActor,
                            idPublicacion
                    );

            return ResponseEntity.ok(respuesta);

        } catch (AccessDeniedException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (NoSuchElementException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PatchMapping("/{idPublicacion}/modo-aceptacion")
    public ResponseEntity<?> actualizarModoAceptacion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idActor,
            @PathVariable UUID idPublicacion,
            @Valid @RequestBody ConfigurarModoAceptacionRequest solicitud,
            BindingResult resultadoValidacion) {

        if (idActor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para configurar el modo de aceptación."
                    ));
        }

        if (resultadoValidacion.hasErrors()) {

            String mensaje = resultadoValidacion.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.joining(" "));

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", mensaje));
        }

        try {

            ModoAceptacionPublicacionResponse respuesta =
                    modoAceptacionService.actualizarModoAceptacion(
                            idActor,
                            idPublicacion,
                            solicitud
                    );

            return ResponseEntity.ok(respuesta);

        } catch (AccessDeniedException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", e.getMessage()));

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
}
