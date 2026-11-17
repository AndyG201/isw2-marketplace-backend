package co.edu.unbosque.marketplace.reporte.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.reporte.api.response.DashboardIndicadoresResponse;
import co.edu.unbosque.marketplace.reporte.application.DashboardIndicadoresService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardIndicadoresController {

    private final DashboardIndicadoresService dashboard;

    public DashboardIndicadoresController(DashboardIndicadoresService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/indicadores")
    public ResponseEntity<?> consultar(Authentication autenticacion) {
        boolean esAdministrador = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_ADMINISTRADOR".equals(autoridad.getAuthority()));

        if (!esAdministrador) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Solo un administrador puede consultar el dashboard."));
        }

        DashboardIndicadoresResponse respuesta = dashboard.consultar();
        return ResponseEntity.ok(respuesta);
    }
}
