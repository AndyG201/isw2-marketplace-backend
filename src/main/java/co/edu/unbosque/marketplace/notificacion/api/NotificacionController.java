
package co.edu.unbosque.marketplace.notificacion.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.notificacion.application.NotificacionConsultaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/notificaciones")
@SecurityRequirement(name = "bearerAuth")
public class NotificacionController {

    private final NotificacionConsultaService consultaService;

    public NotificacionController(
            NotificacionConsultaService consultaService) {

        this.consultaService = consultaService;
    }

    @GetMapping("/mis-notificaciones")
    public ResponseEntity<?> consultarMisNotificaciones(
            @AuthenticationPrincipal(expression = "idUsuario")
            UUID idUsuario) {

        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensaje",
                            "Debes iniciar sesión para consultar tus notificaciones."
                    ));
        }

        return ResponseEntity.ok(
                consultaService.consultarMisNotificaciones(idUsuario)
        );
    }
}
