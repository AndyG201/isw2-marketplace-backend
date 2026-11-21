package co.edu.unbosque.marketplace.usuario.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import co.edu.unbosque.marketplace.usuario.api.request.RestringirUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.api.response.AdministradorUsuarioResponse;
import co.edu.unbosque.marketplace.usuario.application.AdministradorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controlador REST que expone las operaciones administrativas
 * relacionadas con la gestión de usuarios.
 *
 * Permite consultar usuarios, reactivar bloqueos de acceso,
 * restringir cuentas y levantar restricciones administrativas.
 *
 * Requiere autenticación mediante token JWT.
 */
@Tag(
    name = "Administración de usuarios",
    description = "Operaciones administrativas para consultar, restringir y gestionar el acceso de los usuarios."
)
@RestController
@RequestMapping("/administracion/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class AdministradorUsuarioController {

    private final AdministradorService administradorService;

    public AdministradorUsuarioController(
            AdministradorService administradorService) {

        this.administradorService = administradorService;
    }

    @Operation(
        operationId = "reactivarBloqueoUsuario",
        summary = "Reactivar bloqueo de usuario",
        description = "Permite al administrador reactivar el acceso de una cuenta identificada mediante su correo electrónico, cuando se cumplen las condiciones establecidas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "La cuenta ha sido reactivada correctamente.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El correo no cumple las validaciones requeridas."
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No se encontró la cuenta asociada al correo proporcionado."
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Existe un conflicto que impide reactivar el bloqueo."
        )
    })
    @PatchMapping("/reactivar-bloqueo")
    public ResponseEntity<String> reactivarBloqueo(
            @Parameter(
                description = "Correo electrónico de la cuenta cuyo bloqueo se desea reactivar.",
                required = true,
                example = "usuario@correo.com"
            )
            @RequestParam("correo")
            @NotBlank(message = "Debes proporcionar un correo.")
            @Email(message = "El correo no tiene un formato válido.")
            @Size(max = 254, message = "El correo supera la longitud permitida.")
            String correo) {

        try {
            administradorService.reactivarBloqueo(correo.trim());

            return ResponseEntity.ok(
                    "La cuenta ha sido reactivada correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @Operation(
        operationId = "restringirUsuario",
        summary = "Restringir usuario",
        description = "Permite al administrador aplicar una restricción a una cuenta específica, proporcionando los datos requeridos para justificar o configurar la restricción."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "La cuenta ha sido restringida correctamente.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El identificador o los datos de la solicitud no son válidos."
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Existe un conflicto que impide aplicar la restricción."
        )
    })
    @PatchMapping("/{idUsuario}/restringir")
    public ResponseEntity<String> restringirUsuario(
            @Parameter(
                description = "Identificador UUID del usuario que será restringido.",
                required = true
            )
            @PathVariable UUID idUsuario,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Información requerida para aplicar la restricción administrativa.",
                required = true
            )
            @Valid @RequestBody RestringirUsuarioRequest solicitud) {

        try {
            administradorService.restringirUsuario(
                    idUsuario,
                    solicitud
            );

            return ResponseEntity.ok(
                    "La cuenta ha sido restringida correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @Operation(
        operationId = "levantarRestriccionUsuario",
        summary = "Levantar restricción de usuario",
        description = "Permite al administrador retirar una restricción administrativa aplicada previamente a una cuenta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "La restricción administrativa ha sido levantada correctamente.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El identificador del usuario no es válido."
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Existe un conflicto que impide levantar la restricción."
        )
    })
    @PatchMapping("/{idUsuario}/levantar-restriccion")
    public ResponseEntity<String> levantarRestriccion(
            @Parameter(
                description = "Identificador UUID del usuario al que se le retirará la restricción.",
                required = true
            )
            @PathVariable UUID idUsuario) {

        try {
            administradorService.levantarRestriccion(idUsuario);

            return ResponseEntity.ok(
                    "La restricción administrativa ha sido levantada correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @Operation(
        operationId = "consultarUsuariosAdministracion",
        summary = "Consultar usuarios",
        description = "Obtiene el listado de usuarios registrados en Marketplace para su consulta y gestión administrativa."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Listado de usuarios consultado correctamente.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(
                        implementation = AdministradorUsuarioResponse.class
                    )
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<AdministradorUsuarioResponse>> consultarUsuarios() {

        List<AdministradorUsuarioResponse> usuarios =
                administradorService.consultarUsuarios();

        return ResponseEntity.ok(usuarios);
    }
}
