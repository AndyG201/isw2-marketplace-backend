package co.edu.unbosque.marketplace.usuario.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.usuario.api.request.RestablecerContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.api.request.SolicitarRecuperacionRequest;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Controlador REST encargado del proceso de recuperación de contraseña.
 *
 * Permite solicitar un código de recuperación y utilizarlo posteriormente
 * para establecer una nueva contraseña.
 */
@Tag(
    name = "Recuperación de contraseña",
    description = "Operaciones para solicitar la recuperación y restablecer la contraseña de los usuarios."
)
@RestController
@RequestMapping("/recuperacion-contrasena")
public class RecuperacionContrasenaController {

    private final UsuarioService usuarioService;

    public RecuperacionContrasenaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
        operationId = "solicitarRecuperacionContrasena",
        summary = "Solicitar recuperación de contraseña",
        description = "Recibe el correo electrónico del usuario y solicita la generación de un código de recuperación. La respuesta no revela si el correo está registrado en la plataforma."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Solicitud procesada. Si el correo está registrado, se enviarán las instrucciones de recuperación.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    example = "{\"mensaje\":\"Si el correo está registrado, recibirás instrucciones para recuperar tu contraseña.\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "La solicitud enviada no es válida.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    example = "{\"mensaje\":\"Solicitud inválida.\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "No fue posible procesar la solicitud por un error interno.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object")
            )
        )
    })
    @PostMapping("/solicitar")
    public ResponseEntity<Map<String, String>> solicitar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Correo electrónico asociado a la cuenta para solicitar la recuperación de contraseña.",
                required = true
            )
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

    @Operation(
        operationId = "restablecerContrasena",
        summary = "Restablecer contraseña",
        description = "Valida el código de recuperación asociado al correo electrónico y, si es válido y no ha vencido, actualiza la contraseña del usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "La contraseña se actualizó correctamente.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    example = "{\"mensaje\":\"La contraseña se ha actualizado correctamente.\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El código es inválido, ha vencido o la solicitud no pudo completarse.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object",
                    example = "{\"mensaje\":\"El código es inválido, ha vencido o no se pudo completar la recuperación.\"}"
                )
            )
        )
    })
    @PostMapping("/restablecer")
    public ResponseEntity<Map<String, String>> restablecer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Correo electrónico, código de recuperación y nueva contraseña.",
                required = true
            )
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
