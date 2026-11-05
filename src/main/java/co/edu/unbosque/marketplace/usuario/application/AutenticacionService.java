
package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;

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

    private static final int MAX_INTENTOS = 5;

    private static final String MOTIVO_BLOQUEO = "Bloqueo automático por intentos fallidos";

    public AutenticacionService(
            UsuarioRepository usuarioRepository,
            ProteccionContrasena proteccionContrasena,
            JwtUtil jwtUtil) {

        this.usuarioRepository = usuarioRepository;
        this.proteccionContrasena = proteccionContrasena;
        this.jwtUtil = jwtUtil;
    }

    public String iniciarSesion(AutenticacionRequest solicitud) {

        if (solicitud == null
                || solicitud.getCorreo() == null
                || solicitud.getCorreo().isBlank()
                || solicitud.getContrasena() == null
                || solicitud.getContrasena().isBlank()) {

            throw new IllegalArgumentException(
                    "Debes proporcionar el correo y la contraseña."
            );
        }

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(solicitud.getCorreo().trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Correo o contraseña incorrectos."
                ));

        if (usuario.isRestringido()) {

            throw new IllegalStateException(
                    "Tu cuenta tiene una restricción administrativa. "
                    + "No puedes iniciar sesión."
            );
        }

        if (usuario.getIntentosFallidos() >= MAX_INTENTOS) {

            throw new IllegalStateException(
                    "Tu cuenta está bloqueada por intentos fallidos. "
                    + "Debes solicitar su reactivación a un Administrador."
            );
        }

        if (!usuario.isCorreoVerificado()
                || usuario.getEstado() == EstadoUsuario.PENDIENTE_VERIFICACION) {

            throw new IllegalStateException(
                    "Debes verificar tu correo antes de iniciar sesión."
            );
        }

        if (usuario.getEstado() == EstadoUsuario.INACTIVA) {

            throw new IllegalStateException(
                    "Tu cuenta está desactivada."
            );
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVA) {

            throw new IllegalStateException(
                    "La cuenta no se encuentra habilitada para iniciar sesión."
            );
        }

        if (!proteccionContrasena.verificar(
                solicitud.getContrasena(),
                usuario.getContrasenaHash())) {

            registrarIntentoFallido(usuario);

            if (usuario.getIntentosFallidos() >= MAX_INTENTOS) {

                throw new IllegalStateException(
                        "Tu cuenta ha sido bloqueada por alcanzar "
                        + "cinco intentos fallidos. "
                        + "Debes solicitar su reactivación a un Administrador."
                );
            }

            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos."
            );
        }

        if (usuario.getRol() == null
                || usuario.getRol().getNombre() == null
                || usuario.getRol().getNombre().isBlank()) {

            throw new IllegalStateException(
                    "La cuenta no tiene un rol asignado."
            );
        }

        if (usuario.getIntentosFallidos() > 0) {

            usuario.setIntentosFallidos(0);
            usuario.setMotivoBloqueo(null);
            usuario.setFechaActualizacion(OffsetDateTime.now());

            usuarioRepository.save(usuario);
        }

        return jwtUtil.generateToken(usuario);
    }

    private void registrarIntentoFallido(Usuario usuario) {

        int nuevosIntentos = usuario.getIntentosFallidos() + 1;

        usuario.setIntentosFallidos(nuevosIntentos);

        usuario.setFechaActualizacion(OffsetDateTime.now());

        if (nuevosIntentos >= MAX_INTENTOS) {
            usuario.setMotivoBloqueo(MOTIVO_BLOQUEO);
        }

        usuarioRepository.save(usuario);
    }
}
