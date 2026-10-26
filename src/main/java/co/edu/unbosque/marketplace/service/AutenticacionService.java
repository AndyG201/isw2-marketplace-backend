
package co.edu.unbosque.marketplace.service;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.model.Usuario;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.request.AutenticacionRequest;
import co.edu.unbosque.marketplace.security.JwtUtil;
import co.edu.unbosque.marketplace.security.ProteccionContrasena;

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

    public String iniciarSesion(AutenticacionRequest solicitud) {

        if (solicitud == null
                || solicitud.getCorreo() == null
                || solicitud.getCorreo().isBlank()
                || solicitud.getContrasena() == null
                || solicitud.getContrasena().isBlank()) {

            throw new IllegalArgumentException(
                    "Debes proporcionar el correo y la contraseña");
        }

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(solicitud.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Correo o contraseña incorrectos"));

        if (!proteccionContrasena.verificar(
                solicitud.getContrasena(),
                usuario.getContrasenaHash())) {

            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos");
        }

        if (!usuario.isCorreoVerificado()) {
            throw new IllegalStateException(
                    "Debes verificar tu correo antes de iniciar sesión");
        }

        if (usuario.isRestringido()) {
            throw new IllegalStateException(
                    "Tu cuenta tiene restringido el acceso");
        }

        return jwtUtil.generateToken(usuario);
    }
}
