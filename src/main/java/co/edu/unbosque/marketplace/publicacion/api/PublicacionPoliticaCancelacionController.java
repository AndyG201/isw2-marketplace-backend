package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.Objects;
import java.util.NoSuchElementException;
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

import co.edu.unbosque.marketplace.publicacion.api.request.SeleccionarPoliticaCancelacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionPoliticaCancelacionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionPoliticaCancelacionController {

    private final PublicacionPoliticaCancelacionService politicaService;

    public PublicacionPoliticaCancelacionController(
            PublicacionPoliticaCancelacionService politicaService) {

        this.politicaService = politicaService;
    }

    @GetMapping("/{idPublicacion}/politica-cancelacion")
    public ResponseEntity<?> consultarPolitica(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idPublicacion) {

        if (idAnfitrion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para consultar la política."
                    ));
        }

        try {

            return ResponseEntity.ok(
                    politicaService.consultarPolitica(
                            idAnfitrion,
                            idPublicacion
                    )
            );

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

    @PatchMapping("/{idPublicacion}/politica-cancelacion")
    public ResponseEntity<?> seleccionarPolitica(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idPublicacion,
            @Valid @RequestBody SeleccionarPoliticaCancelacionRequest solicitud,
            BindingResult resultadoValidacion) {

        if (idAnfitrion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para seleccionar una política."
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

            return ResponseEntity.ok(
                    politicaService.seleccionarPolitica(
                            idAnfitrion,
                            idPublicacion,
                            solicitud.getIdPolitica()
                    )
            );

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
