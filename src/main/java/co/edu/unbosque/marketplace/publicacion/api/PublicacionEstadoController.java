package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.application.PublicacionEstadoService;
import co.edu.unbosque.marketplace.publicacion.application.dto.EstadoPublicacionResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionEstadoController {

    private final PublicacionEstadoService estadoService;

    public PublicacionEstadoController(
            PublicacionEstadoService estadoService) {

        this.estadoService = estadoService;
    }

    @PatchMapping("/{idPublicacion}/pausar")
    public ResponseEntity<?> pausarPublicacion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idPublicacion) {

        if (idAnfitrion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para pausar publicaciones."
                    ));
        }

        EstadoPublicacionResponse respuesta =
                estadoService.pausarPublicacion(
                        idAnfitrion,
                        idPublicacion
                );

        return ResponseEntity.ok(respuesta);
    }

    @PatchMapping("/{idPublicacion}/reactivar")
    public ResponseEntity<?> reactivarPublicacion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idPublicacion) {

        if (idAnfitrion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para reactivar publicaciones."
                    ));
        }

        EstadoPublicacionResponse respuesta =
                estadoService.reactivarPublicacion(
                        idAnfitrion,
                        idPublicacion
                );

        return ResponseEntity.ok(respuesta);
    }
}
