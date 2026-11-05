
package co.edu.unbosque.marketplace.cancelacion.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.cancelacion.api.response.PoliticaCancelacionResponse;
import co.edu.unbosque.marketplace.cancelacion.application.PoliticaCancelacionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/politicas-cancelacion")
@SecurityRequirement(name = "bearerAuth")
public class PoliticaCancelacionController {

    private final PoliticaCancelacionService politicaService;

    public PoliticaCancelacionController(
            PoliticaCancelacionService politicaService) {

        this.politicaService = politicaService;
    }

    @GetMapping("/activas")
    public ResponseEntity<List<PoliticaCancelacionResponse>>
            consultarPoliticasActivas() {

        return ResponseEntity.ok(
                politicaService.consultarPoliticasActivas()
        );
    }
}
