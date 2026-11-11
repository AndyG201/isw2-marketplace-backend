package co.edu.unbosque.marketplace.publicacion.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionCatalogoService;

@RestController
@RequestMapping("/publicaciones")
public class PublicacionCatalogoController {

    private final PublicacionCatalogoService catalogoService;

    public PublicacionCatalogoController(
            PublicacionCatalogoService catalogoService) {

        this.catalogoService = catalogoService;
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<PublicacionResumenResponse>>
            consultarCatalogo() {

        return ResponseEntity.ok(
                catalogoService.consultarCatalogo()
        );
    }
}
