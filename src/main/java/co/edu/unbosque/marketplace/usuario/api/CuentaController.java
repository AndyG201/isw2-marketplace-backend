package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.usuario.api.request.ConfirmarCambioCorreoRequest;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;
import co.edu.unbosque.marketplace.usuario.application.dto.ActualizarContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.ActualizarPerfilRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.ConsultarCuentaResponse;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controlador REST encargado de gestionar las operaciones de consulta,
 * actualización y administración de la cuenta del usuario autenticado.
 *
 * Todas las operaciones utilizan la identidad del usuario obtenida
 * del contexto de seguridad de Spring.
 */
@Tag(
    name = "Gestión de cuenta",
    description = "Consulta y actualización de información personal, correo, contraseña y estado de la cuenta."
)
@RestController
@RequestMapping("/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class CuentaController {

    private final UsuarioService usuarioService;

    public CuentaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
        operationId = "consultarMiCuenta",
        summary = "Consultar mi cuenta",
        description = "Obtiene la información de la cuenta correspondiente al usuario autenticado mediante su token JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información de la cuenta consultada correctamente.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConsultarCuentaResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No existe un usuario autenticado."
        )
    })
    @GetMapping("/mi_cuenta")
    public ResponseEntity<?> consultarMiCuenta(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Usuario usuario) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ConsultarCuentaResponse respuesta =
                usuarioService.consultarMiCuenta(usuario.getIdUsuario());

        return ResponseEntity.ok(respuesta);
    }

    @Operation(
        operationId = "actualizarPerfil",
        summary = "Actualizar perfil",
        description = "Permite modificar la información del perfil del usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Perfil actualizado correctamente.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos del perfil no son válidos."
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No existe un usuario autenticado."
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Existe un conflicto que impide actualizar el perfil."
        )
    })
    @PutMapping("/mi_cuenta/perfil")
    public ResponseEntity<String> actualizarPerfil(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Usuario usuario,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Información del perfil que se desea actualizar.",
                required = true
            )
            @Valid @RequestBody ActualizarPerfilRequest solicitud) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String resultado = usuarioService.actualizarPerfil(
                    usuario.getIdUsuario(), solicitud);

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Operation(
        operationId = "confirmarCambioCorreo",
        summary = "Confirmar cambio de correo electrónico",
        description = "Valida el código de confirmación enviado al usuario para completar la actualización y verificación de su correo electrónico."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Correo electrónico actualizado y verificado correctamente."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El código o los datos enviados no son válidos."
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No existe un usuario autenticado."
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Existe un conflicto que impide confirmar el cambio."
        )
    })
    @PostMapping("/mi_cuenta/correo/confirmar")
    public ResponseEntity<String> confirmarCambioCorreo(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Usuario usuario,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Código de verificación requerido para confirmar el nuevo correo electrónico.",
                required = true
            )
            @Valid @RequestBody ConfirmarCambioCorreoRequest solicitud) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            usuarioService.confirmarCambioCorreo(
                    usuario.getIdUsuario(), solicitud.getCodigo());

            return ResponseEntity.ok(
                    "El correo electrónico se actualizó y verificó correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @Operation(
        operationId = "actualizarContrasena",
        summary = "Actualizar contraseña",
        description = "Permite al usuario autenticado cambiar su contraseña mediante los datos de validación proporcionados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Contraseña actualizada correctamente."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos proporcionados no son válidos o no cumplen las condiciones requeridas."
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No existe un usuario autenticado."
        )
    })
    @PatchMapping("/mi_cuenta/contrasena")
    public ResponseEntity<String> actualizarContrasena(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Usuario usuario,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos necesarios para realizar el cambio de contraseña.",
                required = true
            )
            @Valid @RequestBody ActualizarContrasenaRequest solicitud) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            usuarioService.actualizarContrasena(
                    usuario.getIdUsuario(), solicitud);

            return ResponseEntity.ok("La contraseña se actualizó correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
        operationId = "desactivarCuenta",
        summary = "Desactivar mi cuenta",
        description = "Permite al usuario autenticado solicitar la desactivación de su propia cuenta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cuenta desactivada correctamente."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "No se pudo realizar la desactivación debido a datos o condiciones inválidas."
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No existe un usuario autenticado."
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Existe un conflicto que impide desactivar la cuenta."
        )
    })
    @PatchMapping("/mi_cuenta/desactivar")
    public ResponseEntity<String> desactivarCuenta(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Usuario usuario) {

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            usuarioService.desactivarCuenta(usuario.getIdUsuario());

            return ResponseEntity.ok(
                    "Tu cuenta ha sido desactivada correctamente.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
