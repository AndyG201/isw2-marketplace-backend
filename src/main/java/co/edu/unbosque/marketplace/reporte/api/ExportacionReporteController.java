package co.edu.unbosque.marketplace.reporte.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.marketplace.reporte.api.request.SolicitarExportacionRequest;
import co.edu.unbosque.marketplace.reporte.api.response.ExportacionReporteResponse;
import co.edu.unbosque.marketplace.reporte.application.ExportacionReporteService;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/administracion/reportes/exportaciones")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class ExportacionReporteController {

    private final ExportacionReporteService servicio;

    public ExportacionReporteController(ExportacionReporteService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<ExportacionReporteResponse> solicitar(
            @Valid @RequestBody SolicitarExportacionRequest request,
            Authentication autenticacion) {

        UUID idAdministrador = obtenerIdAdministrador(autenticacion);
        ExportacionReporteResponse respuesta =
                servicio.solicitar(idAdministrador, request.formato());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(respuesta);
    }

    @GetMapping("/{idExportacion}")
    public ResponseEntity<ExportacionReporteResponse> consultar(
            @PathVariable UUID idExportacion,
            Authentication autenticacion) {

        UUID idAdministrador = obtenerIdAdministrador(autenticacion);
        return ResponseEntity.ok(
                servicio.consultar(idExportacion, idAdministrador));
    }

    @GetMapping("/{idExportacion}/archivo")
    public ResponseEntity<byte[]> descargar(
            @PathVariable UUID idExportacion,
            Authentication autenticacion) {

        UUID idAdministrador = obtenerIdAdministrador(autenticacion);
        return servicio.descargar(idExportacion, idAdministrador);
    }

    private UUID obtenerIdAdministrador(Authentication autenticacion) {
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof Usuario usuario)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Se requiere una sesión de administrador.");
        }

        boolean esAdministrador = usuario.getAuthorities().stream()
                .anyMatch(autoridad ->
                        "ROLE_ADMINISTRADOR".equals(autoridad.getAuthority()));

        if (!esAdministrador) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Solo un administrador puede exportar reportes.");
        }

        return usuario.getIdUsuario();
    }
}