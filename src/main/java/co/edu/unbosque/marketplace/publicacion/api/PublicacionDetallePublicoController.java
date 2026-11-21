package co.edu.unbosque.marketplace.publicacion.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.application.PublicacionDetallePublicoService;

@RestController
@RequestMapping("/publicaciones")
public class PublicacionDetallePublicoController {

    private final PublicacionDetallePublicoService detallePublicoService;

    public PublicacionDetallePublicoController(PublicacionDetallePublicoService detallePublicoService) {
        this.detallePublicoService = detallePublicoService;
    }

    @GetMapping("/{idPublicacion}/detalle-publico")
    public ResponseEntity<?> consultar(@PathVariable UUID idPublicacion) {
        return ResponseEntity.ok(detallePublicoService.consultar(idPublicacion));
    }
}
