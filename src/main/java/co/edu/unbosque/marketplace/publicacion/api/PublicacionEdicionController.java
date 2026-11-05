
package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.publicacion.api.request.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionEdicionService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

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
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID idPublicacion,
            @Valid @ModelAttribute EditarPublicacionRequest solicitud,
            BindingResult resultadoValidacion) {

        if (usuario == null) {
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

        try {

            PublicacionAlojamientoResponse respuesta =
                    edicionService.actualizarPublicacion(
                            usuario,
                            idPublicacion,
                            solicitud
                    );

            return ResponseEntity.ok(respuesta);

        } catch (AccessDeniedException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (java.util.NoSuchElementException e) {

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
