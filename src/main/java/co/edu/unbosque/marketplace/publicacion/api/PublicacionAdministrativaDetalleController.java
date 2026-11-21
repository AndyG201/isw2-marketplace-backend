package co.edu.unbosque.marketplace.publicacion.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.application.PublicacionDetallePublicoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionAdministrativaDetalleController {

    private final PublicacionDetallePublicoService detalleService;

    public PublicacionAdministrativaDetalleController(PublicacionDetallePublicoService detalleService) {
        this.detalleService = detalleService;
    }

    @GetMapping("/{idPublicacion}")
    public ResponseEntity<?> consultar(@PathVariable UUID idPublicacion) {
        return ResponseEntity.ok(detalleService.consultarParaAdministracion(idPublicacion));
    }
}
