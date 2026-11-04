
package co.edu.unbosque.marketplace.usuario.application;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.usuario.api.request.AutenticacionRequest;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.ProteccionContrasena;

@Service
public class AutenticacionService {

    private final UsuarioRepository usuarioRepository;
    private final ProteccionContrasena proteccionContrasena;
    private final JwtUtil jwtUtil;

    public AutenticacionService(
            UsuarioRepository usuarioRepository,
            ProteccionContrasena proteccionContrasena,
            JwtUtil jwtUtil) {

        this.usuarioRepository = usuarioRepository;
        this.proteccionContrasena = proteccionContrasena;
        this.jwtUtil = jwtUtil;
    }

    // =====================================================
    // INICIAR SESIÓN - HU-002
    // =====================================================

    public String iniciarSesion(AutenticacionRequest solicitud) {

        // 1. Validar campos obligatorios.
        if (solicitud == null
                || solicitud.getCorreo() == null
                || solicitud.getCorreo().isBlank()
                || solicitud.getContrasena() == null
                || solicitud.getContrasena().isBlank()) {

            throw new IllegalArgumentException(
                    "Debes proporcionar el correo y la contraseña."
            );
        }

        // 2. Buscar usuario por correo.
        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(solicitud.getCorreo().trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Correo o contraseña incorrectos."
                ));

        // 3. Verificar contraseña.
        if (!proteccionContrasena.verificar(
                solicitud.getContrasena(),
                usuario.getContrasenaHash())) {

            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos."
            );
        }

        // 4. Verificar que el correo esté confirmado.
        if (!usuario.isCorreoVerificado()
                || usuario.getEstado() == EstadoUsuario.PENDIENTE_VERIFICACION) {

            throw new IllegalStateException(
                    "Debes verificar tu correo antes de iniciar sesión."
            );
        }

        // 5. Verificar restricción administrativa.
        if (usuario.isRestringido()) {

            throw new IllegalStateException(
                    "Tu cuenta tiene una restricción administrativa. "
                    + "No puedes iniciar sesión."
            );
        }

        // 6. Verificar bloqueo por intentos fallidos.
        if (usuario.getIntentosFallidos() >= 5) {

            throw new IllegalStateException(
                    "Tu cuenta está bloqueada por intentos fallidos. "
                    + "Debes solicitar su reactivación a un Administrador."
            );
        }

        // 7. Verificar si el usuario desactivó su cuenta.
        if (usuario.getEstado() == EstadoUsuario.INACTIVA) {

            throw new IllegalStateException(
                    "Tu cuenta está desactivada. "
                    + "Solo puedes acceder a la opción de reactivarla."
            );
        }

        // 8. Comprobar que la cuenta esté activa.
        if (usuario.getEstado() != EstadoUsuario.ACTIVA) {

            throw new IllegalStateException(
                    "La cuenta no se encuentra habilitada para iniciar sesión."
            );
        }

        // 9. Comprobar que tenga un rol asignado.
        if (usuario.getRol() == null
                || usuario.getRol().getNombre() == null
                || usuario.getRol().getNombre().isBlank()) {

            throw new IllegalStateException(
                    "La cuenta no tiene un rol asignado."
            );
        }

        // 10. Restablecer intentos fallidos después de un
        // inicio de sesión exitoso.
        if (usuario.getIntentosFallidos() > 0) {

            usuario.setIntentosFallidos(0);
            usuario.setFechaActualizacion(
                    java.time.OffsetDateTime.now()
            );

            usuarioRepository.save(usuario);
        }

        // 11. Generar JWT con la identificación del rol.
        return jwtUtil.generateToken(usuario);
    }
}
