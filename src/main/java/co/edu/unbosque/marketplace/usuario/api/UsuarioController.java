package co.edu.unbosque.marketplace.usuario.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.usuario.application.dto.AutenticacionRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.RegistrarUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.ReactivarCuentaRequest;
import co.edu.unbosque.marketplace.usuario.api.response.AutenticacionResponse;
import co.edu.unbosque.marketplace.usuario.application.AutenticacionService;
import co.edu.unbosque.marketplace.usuario.application.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Controlador REST encargado de gestionar las operaciones de registro,
 * autenticación y reactivación de cuentas de usuario.
 *
 * Expone los endpoints públicos relacionados con el acceso al sistema
 * Marketplace y delega la ejecución de las operaciones en los servicios
 * de aplicación correspondientes.
 *
 * Los endpoints de esta clase no requieren autenticación JWT previa.
 */
@Tag(
    name = "Usuarios y autenticación",
    description = "Endpoints para el registro de usuarios, inicio de sesión y reactivación de cuentas."
)
@RestController
@RequestMapping("/autenticacion")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AutenticacionService autenticacionService;

    /**
     * Construye el controlador con los servicios necesarios para gestionar
     * las operaciones de usuario y autenticación.
     *
     * @param usuarioService servicio encargado de las operaciones de usuario.
     * @param autenticacionService servicio encargado de autenticar usuarios.
     */
    public UsuarioController(
            UsuarioService usuarioService,
            AutenticacionService autenticacionService) {

        this.usuarioService = usuarioService;
        this.autenticacionService = autenticacionService;
    }

    /**
     * Registra un nuevo usuario en la plataforma Marketplace.
     *
     * Recibe los datos de registro, valida la información y delega
     * la creación de la cuenta al servicio de usuarios.
     *
     * @param nuevoUsuario información requerida para registrar al usuario.
     * @return respuesta indicando si el registro fue exitoso o si los datos
     *         enviados no pudieron procesarse.
     */
    @Operation(
        operationId = "registrarUsuario",
        summary = "Registrar un usuario",
        description = "Permite crear una nueva cuenta en Marketplace mediante los datos personales y credenciales enviados. Después del registro, el usuario debe verificar su correo electrónico para activar el acceso."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario registrado correctamente. Se debe verificar el correo electrónico para activar el acceso.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos enviados no cumplen las validaciones requeridas o el usuario ya existe."
        )
    })
    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Información personal y credenciales necesarias para crear una cuenta de usuario.",
                required = true
            )
            @Valid @RequestBody RegistrarUsuarioRequest nuevoUsuario) {

        int resultado = usuarioService.registrar(nuevoUsuario);

        if (resultado == 1) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario creado. Verifica tu correo para activar el acceso.");
        }

        return ResponseEntity.badRequest()
                .body("Datos inválidos o usuario existente.");
    }

    /**
     * Autentica a un usuario mediante sus credenciales.
     *
     * Si las credenciales son válidas y el acceso está permitido,
     * genera y devuelve un token JWT para utilizar los recursos protegidos
     * de la plataforma.
     *
     * @param solicitud correo electrónico y contraseña del usuario.
     * @return token JWT cuando la autenticación es exitosa o una respuesta
     *         de error cuando las credenciales o el estado de la cuenta
     *         impiden el acceso.
     */
    @Operation(
        operationId = "iniciarSesion",
        summary = "Iniciar sesión",
        description = "Autentica al usuario mediante su correo electrónico y contraseña. Cuando el acceso es autorizado, genera un token JWT que permite consumir los endpoints protegidos de Marketplace."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa. Se devuelve el token JWT.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AutenticacionResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos enviados no cumplen las validaciones requeridas."
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Las credenciales proporcionadas no son válidas.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "El acceso fue rechazado debido al estado de la cuenta o a una restricción de acceso.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenciales del usuario necesarias para realizar la autenticación.",
                required = true
            )
            @Valid @RequestBody AutenticacionRequest solicitud) {

        try {
            String token = autenticacionService.iniciarSesion(solicitud);

            return ResponseEntity.ok(new AutenticacionResponse(token));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    /**
     * Solicita la reactivación de una cuenta de usuario previamente
     * desactivada.
     *
     * Recibe los datos requeridos para verificar la solicitud y delega
     * el proceso de reactivación al servicio de usuarios.
     *
     * @param solicitud información y credenciales requeridas para reactivar
     *                  la cuenta.
     * @return confirmación de la reactivación o respuesta indicando
     *         que la operación no pudo realizarse.
     */
    @Operation(
        operationId = "reactivarCuenta",
        summary = "Reactivar una cuenta desactivada",
        description = "Permite solicitar la reactivación de una cuenta desactivada. El sistema valida los datos y las condiciones de acceso antes de habilitar nuevamente la cuenta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "La cuenta fue reactivada correctamente.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos enviados no cumplen las validaciones requeridas."
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Las credenciales o los datos de verificación no son válidos.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "La reactivación no está permitida debido al estado o las condiciones de la cuenta.",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(implementation = String.class)
            )
        )
    })
    @PostMapping("/reactivar")
    public ResponseEntity<String> reactivarCuenta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos necesarios para verificar la identidad del usuario y solicitar la reactivación de su cuenta.",
                required = true
            )
            @Valid @RequestBody ReactivarCuentaRequest solicitud) {

        try {
            usuarioService.reactivarCuenta(solicitud);

            return ResponseEntity.ok(
                    "Tu cuenta ha sido reactivada correctamente. "
                    + "Inicia sesión nuevamente."
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }
}
