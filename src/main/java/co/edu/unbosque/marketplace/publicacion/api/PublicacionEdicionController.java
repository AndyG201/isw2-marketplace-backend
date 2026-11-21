package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.publicacion.application.PublicacionEdicionService;
import co.edu.unbosque.marketplace.publicacion.application.dto.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionAlojamientoResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionEdicionController {

    private final PublicacionEdicionService edicionService;

    public PublicacionEdicionController(
            PublicacionEdicionService edicionService) {

        this.edicionService = edicionService;
    }

    @PutMapping(
            value = "/{idPublicacion}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> editarPublicacion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idActor,
            @PathVariable UUID idPublicacion,
            @Valid @ModelAttribute EditarPublicacionRequest solicitud,
            BindingResult resultadoValidacion) {

        if (idActor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para modificar publicaciones."
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

        PublicacionAlojamientoResponse respuesta =
                edicionService.actualizarPublicacion(
                        idActor,
                        idPublicacion,
                        solicitud
                );

        return ResponseEntity.ok(respuesta);
    }
}
