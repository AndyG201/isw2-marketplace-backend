package co.edu.unbosque.marketplace.publicacion.api;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.publicacion.application.PublicacionConfiguracionComercialService;
import co.edu.unbosque.marketplace.publicacion.application.dto.ConfigurarConfiguracionComercialRequest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/publicaciones")
@SecurityRequirement(name = "bearerAuth")
public class PublicacionConfiguracionComercialController {

    private final PublicacionConfiguracionComercialService configuracionService;

    public PublicacionConfiguracionComercialController(
            PublicacionConfiguracionComercialService configuracionService) {

        this.configuracionService = configuracionService;
    }

    @GetMapping("/{idPublicacion}/configuracion-comercial")
    public ResponseEntity<?> consultarConfiguracion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idPublicacion) {

        if (idAnfitrion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para consultar la configuración."
                    ));
        }

        return ResponseEntity.ok(
                configuracionService.consultarConfiguracion(
                        idAnfitrion,
                        idPublicacion
                )
        );
    }

    @PutMapping("/{idPublicacion}/configuracion-comercial")
    public ResponseEntity<?> actualizarConfiguracion(
            @AuthenticationPrincipal(expression = "idUsuario") UUID idAnfitrion,
            @PathVariable UUID idPublicacion,
            @Valid @RequestBody ConfigurarConfiguracionComercialRequest solicitud,
            BindingResult resultadoValidacion) {

        if (idAnfitrion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para configurar la publicación."
                    ));
        }

        if (resultadoValidacion.hasErrors()) {

            String mensaje = resultadoValidacion.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.joining(" "));

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", mensaje));
        }

        return ResponseEntity.ok(
                configuracionService.actualizarConfiguracion(
                        idAnfitrion,
                        idPublicacion,
                        solicitud
                )
        );
    }
}
