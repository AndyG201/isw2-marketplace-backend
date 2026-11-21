package co.edu.unbosque.marketplace.reporte.api;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.reporte.application.DashboardIndicadoresService;
import co.edu.unbosque.marketplace.reporte.application.dto.DashboardIndicadoresResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardIndicadoresController {

    private static final Set<String> ROLES_CON_ACCESO =
            Set.of("ROLE_ADMINISTRADOR", "ROLE_AUDITOR_OPERACION");

    private final DashboardIndicadoresService dashboard;

    public DashboardIndicadoresController(DashboardIndicadoresService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/indicadores")
    public ResponseEntity<?> consultar(Authentication autenticacion) {
        boolean autorizado = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> ROLES_CON_ACCESO.contains(autoridad.getAuthority()));

        if (!autorizado) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje",
                            "Solo un administrador o un auditor de operación puede consultar el dashboard."));
        }

        DashboardIndicadoresResponse respuesta = dashboard.consultar();
        return ResponseEntity.ok(respuesta);
    }
}
