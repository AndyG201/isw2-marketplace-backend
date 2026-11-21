package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.publicacion.api.request.RechazarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.RevisionAdministrativaService;
import co.edu.unbosque.marketplace.publicacion.application.dto.DecisionPublicacionResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionDecisionController {

    private final RevisionAdministrativaService revisionService;

    public PublicacionDecisionController(
            RevisionAdministrativaService revisionService) {

        this.revisionService = revisionService;
    }

    @PatchMapping("/{idPublicacion}/aprobar")
    public ResponseEntity<?> aprobarPublicacion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idUsuario,
            @PathVariable UUID idPublicacion) {

        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para realizar esta operación."
                    ));
        }

        DecisionPublicacionResponse respuesta =
                revisionService.aprobarPublicacion(
                        idUsuario,
                        idPublicacion
                );

        return ResponseEntity.ok(respuesta);
    }

    @PatchMapping("/{idPublicacion}/rechazar")
    public ResponseEntity<?> rechazarPublicacion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idUsuario,
            @PathVariable UUID idPublicacion,
            @Valid @RequestBody RechazarPublicacionRequest solicitud,
            BindingResult resultadoValidacion) {

        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para realizar esta operación."
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

        DecisionPublicacionResponse respuesta =
                revisionService.rechazarPublicacion(
                        idUsuario,
                        idPublicacion,
                        solicitud.getMotivo()
                );

        return ResponseEntity.ok(respuesta);
    }
}
