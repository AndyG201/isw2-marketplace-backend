
package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.api.response.EnviarRevisionResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionRevisionService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionRevisionController {

    private final PublicacionRevisionService revisionService;

    public PublicacionRevisionController(
            PublicacionRevisionService revisionService) {

        this.revisionService = revisionService;
    }

    @PatchMapping("/{idPublicacion}/enviar-revision")
    public ResponseEntity<?> enviarRevision(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID idPublicacion) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para enviar publicaciones a revisión."
                    ));
        }

        try {

            EnviarRevisionResponse respuesta =
                    revisionService.enviarRevision(
                            usuario,
                            idPublicacion
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
