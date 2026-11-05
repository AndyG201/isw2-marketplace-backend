
package co.edu.unbosque.marketplace.servicio.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.servicio.api.response.ServicioResponse;
import co.edu.unbosque.marketplace.servicio.application.ServicioService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/servicios")
@SecurityRequirement(name = "bearerAuth")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public ResponseEntity<List<ServicioResponse>> consultarServicios() {

        return ResponseEntity.ok(
                servicioService.consultarServicios()
        );
    }
}
