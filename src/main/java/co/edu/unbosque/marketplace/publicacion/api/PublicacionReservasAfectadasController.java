
package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.application.PublicacionReservasAfectadasService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionReservasAfectadasController {

    private final PublicacionReservasAfectadasService consultaService;

    public PublicacionReservasAfectadasController(
            PublicacionReservasAfectadasService consultaService) {

        this.consultaService = consultaService;
    }

    @GetMapping("/{idPublicacion}/reservas-afectadas")
    public ResponseEntity<?> consultarReservasAfectadas(
            @AuthenticationPrincipal(expression = "idUsuario")
            UUID idAdministrador,
            @PathVariable UUID idPublicacion) {

        if (idAdministrador == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión como administrador."
                    ));
        }

        try {

            return ResponseEntity.ok(
                    consultaService.consultarReservasAfectadas(
                            idPublicacion
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
}
