
package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.api.response.MisPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionConsultaService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/publicaciones/mis-publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionConsultaController {

    private final PublicacionConsultaService consultaService;

    public PublicacionConsultaController(
            PublicacionConsultaService consultaService) {

        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<?> consultarMisPublicaciones(
            @AuthenticationPrincipal Usuario usuario) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para consultar tus publicaciones."
                    ));
        }

        MisPublicacionesResponse respuesta =
                consultaService.consultarMisPublicaciones(
                        usuario.getIdUsuario()
                );

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{idPublicacion}")
    public ResponseEntity<?> consultarDetalle(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable UUID idPublicacion) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para consultar esta publicación."
                    ));
        }

        try {

            PublicacionDetalleResponse respuesta =
                    consultaService.consultarDetalle(
                            usuario.getIdUsuario(),
                            idPublicacion
                    );

            return ResponseEntity.ok(respuesta);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
