
package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.publicacion.api.request.CrearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionAlojamientoService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/alojamientos")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionAlojamientoController {

    private final PublicacionAlojamientoService publicacionService;

    public PublicacionAlojamientoController(
            PublicacionAlojamientoService publicacionService) {

        this.publicacionService = publicacionService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearPublicacion(
            @AuthenticationPrincipal Usuario usuario,
            @Valid @ModelAttribute CrearPublicacionRequest solicitud,
            BindingResult resultadoValidacion) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Debes iniciar sesión."));
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
                    publicacionService.crearPublicacion(
                            usuario.getIdUsuario(),
                            solicitud
                    );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(respuesta);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
