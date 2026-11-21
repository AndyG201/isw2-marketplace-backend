package co.edu.unbosque.marketplace.reporte.api;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.reporte.api.request.SolicitarExportacionRequest;
import co.edu.unbosque.marketplace.reporte.application.ArchivoExportado;
import co.edu.unbosque.marketplace.reporte.application.ExportacionReporteService;
import co.edu.unbosque.marketplace.reporte.application.dto.ExportacionReporteResponse;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionQuery;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/administracion/reportes/exportaciones")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class ExportacionReporteController {

    private final ExportacionReporteService servicio;
    private final UsuarioSesionQuery usuarios;

    public ExportacionReporteController(
            ExportacionReporteService servicio,
            UsuarioSesionQuery usuarios) {
        this.servicio = servicio;
        this.usuarios = usuarios;
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
        ArchivoExportado archivo = servicio.descargar(idExportacion, idAdministrador);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.getTipoContenido()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.getNombre() + "\"")
                .body(archivo.getContenido());
    }

    private UUID obtenerIdAdministrador(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new AccesoNoPermitidoException(
                    "Se requiere una sesión de administrador.");
        }

        UsuarioSesionInfo usuario = usuarios
                .consultarPorCorreo(autenticacion.getName())
                .filter(UsuarioSesionInfo::administrador)
                .orElseThrow(() -> new AccesoNoPermitidoException(
                        "Solo un administrador puede exportar reportes."));

        return usuario.idUsuario();
    }
}