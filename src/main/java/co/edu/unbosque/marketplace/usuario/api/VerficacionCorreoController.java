package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.usuario.application.VerificacionCorreoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Controlador REST encargado de gestionar la verificación del correo
 * electrónico de los usuarios mediante códigos de confirmación.
 *
 * Permite confirmar un código recibido y solicitar el reenvío
 * de un nuevo código de verificación.
 */
@Tag(
    name = "Verificación de correo",
    description = "Operaciones para confirmar y reenviar códigos de verificación del correo electrónico."
)
@RestController
@RequestMapping("/verificacion-correo")
@CrossOrigin(origins = "*")
public class VerficacionCorreoController {

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    @Operation(
        operationId = "confirmarCodigoCorreo",
        summary = "Confirmar código de verificación",
        description = "Valida el código enviado al correo electrónico del usuario y confirma la verificación cuando los datos proporcionados son correctos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Correo electrónico verificado correctamente.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El correo o el código están vacíos, son inválidos o no se pudo completar la verificación.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        )
    })
    @PostMapping("/confirmar")
    public ResponseEntity<String> confirmarCodigo(
            @Parameter(
                description = "Dirección de correo electrónico que se desea verificar.",
                required = true,
                example = "usuario@correo.com"
            )
            @RequestParam("correo") String correo,

            @Parameter(
                description = "Código de verificación recibido en el correo electrónico.",
                required = true,
                example = "123456"
            )
            @RequestParam("codigo") String codigo) {

        if (correo == null || correo.isBlank()
                || codigo == null || codigo.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Debes proporcionar el correo y el código.");
        }

        verificacionCorreoService.confirmarCodigo(
                correo,
                codigo
        );

        return ResponseEntity.ok(
                "Correo verificado correctamente."
        );
    }

    @Operation(
        operationId = "reenviarCodigoCorreo",
        summary = "Reenviar código de verificación",
        description = "Solicita el envío de un nuevo código de verificación al correo electrónico proporcionado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Se envió un nuevo código al correo electrónico.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El correo está vacío o no fue posible reenviar el código.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        )
    })
    @PostMapping("/reenviar")
    public ResponseEntity<String> reenviarCodigo(
            @Parameter(
                description = "Dirección de correo electrónico a la que se enviará el nuevo código.",
                required = true,
                example = "usuario@correo.com"
            )
            @RequestParam("correo") String correo) {

        if (correo == null || correo.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Debes proporcionar un correo.");
        }

        verificacionCorreoService.reenviarCodigo(correo);

        return ResponseEntity.ok(
                "Se envió un nuevo código al correo electrónico."
        );
    }
}
