package co.edu.unbosque.marketplace.tarifa.api;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.tarifa.application.ConfiguracionTarifaService;
import co.edu.unbosque.marketplace.tarifa.application.dto.ConfiguracionTarifaResponse;
import co.edu.unbosque.marketplace.tarifa.application.dto.ConfigurarPorcentajesTarifaRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/administracion/tarifas")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracionTarifaController {

    private final ConfiguracionTarifaService service;

    public ConfiguracionTarifaController(
            ConfiguracionTarifaService service) {
        this.service = service;
    }

    @GetMapping("/configuracion-vigente")
    public ResponseEntity<?> consultarConfiguracionVigente() {
        try {
            ConfiguracionTarifaResponse respuesta =
                    service.consultarParaAdministracion();

            return ResponseEntity.ok(respuesta);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PutMapping("/configuracion-vigente")
    public ResponseEntity<?> configurar(
            @AuthenticationPrincipal(expression = "idUsuario")
            UUID idAdministrador,
            @Valid @RequestBody
            ConfigurarPorcentajesTarifaRequest solicitud) {

        if (idAdministrador == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión como administrador."
                    ));
        }

        try {
            ConfiguracionTarifaResponse respuesta =
                    service.configurar(idAdministrador, solicitud);

            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}