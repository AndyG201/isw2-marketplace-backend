
package co.edu.unbosque.marketplace.usuario.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.usuario.api.request.RestablecerContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.api.request.SolicitarRecuperacionRequest;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;

@RestController
@RequestMapping("/recuperacion-contrasena")
public class RecuperacionContrasenaController {

    private final UsuarioService usuarioService;

    public RecuperacionContrasenaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<Map<String, String>> solicitar(
            @Valid @RequestBody SolicitarRecuperacionRequest solicitud) {

        if (solicitud == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Solicitud inválida."));
        }

        try {
            usuarioService.generarCodigoRecuperacion(
                    solicitud.getCorreo().trim()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "mensaje",
                            "No fue posible procesar la solicitud en este momento."
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "mensaje",
                "Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña."
        ));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecer(
            @Valid @RequestBody RestablecerContrasenaRequest solicitud) {

        if (solicitud == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Solicitud inválida."));
        }

        boolean resultado = usuarioService.restablecerContrasena(
                solicitud.getCorreo().trim(),
                solicitud.getCodigo(),
                solicitud.getNuevaContrasena()
        );

        if (!resultado) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "mensaje",
                            "El código es inválido, ha vencido o no se pudo completar la recuperación."
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "mensaje",
                "La contraseña se ha actualizado correctamente."
        ));
    }
}
