package co.edu.unbosque.marketplace.reporte.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.reporte.application.IngresosAnfitrionService;
import co.edu.unbosque.marketplace.reporte.application.dto.IngresosAnfitrionResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/** Lo que ha generado el anfitrión que consulta, sin mostrar nada del resto de la plataforma. */
@RestController
@RequestMapping("/anfitrion/ingresos")
@SecurityRequirement(name = "bearerAuth")
public class IngresosAnfitrionController {

    private final IngresosAnfitrionService ingresos;

    public IngresosAnfitrionController(IngresosAnfitrionService ingresos) {
        this.ingresos = ingresos;
    }

    @GetMapping
    public ResponseEntity<IngresosAnfitrionResponse> consultar(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            Authentication autenticacion) {
        boolean esAnfitrion = autenticacion != null
                && autenticacion.getAuthorities().stream()
                        .anyMatch(autoridad -> "ROLE_ANFITRION".equals(autoridad.getAuthority()));
        if (!esAnfitrion) {
            throw new AccesoNoPermitidoException("Solo un anfitrión puede consultar sus ingresos.");
        }
        return ResponseEntity.ok(ingresos.consultar(idAnfitrion));
    }
}
