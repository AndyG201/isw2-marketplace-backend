package co.edu.unbosque.marketplace.resena.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.resena.application.ResenaPublicaQueryService;
import co.edu.unbosque.marketplace.resena.application.dto.ResenasPublicacionResponse;

@RestController
@RequestMapping("/publicaciones")
public class ResenaPublicaController {

    private final ResenaPublicaQueryService consulta;

    public ResenaPublicaController(ResenaPublicaQueryService consulta) {
        this.consulta = consulta;
    }

    @GetMapping("/{idPublicacion}/resenas")
    public ResponseEntity<ResenasPublicacionResponse> consultar(
            @PathVariable UUID idPublicacion) {
        return ResponseEntity.ok(consulta.consultar(idPublicacion));
    }
}
