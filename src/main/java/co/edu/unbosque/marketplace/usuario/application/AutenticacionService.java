
package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
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
    private AuditoriaEventoCommand auditoria;

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

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
    }

    public String iniciarSesion(AutenticacionRequest solicitud) {

        if (solicitud == null
                || solicitud.getCorreo() == null
                || solicitud.getCorreo().isBlank()
                || solicitud.getContrasena() == null
                || solicitud.getContrasena().isBlank()) {

            registrarResultado(null, "FALLIDO", "CREDENCIALES_INCOMPLETAS");
            throw new IllegalArgumentException(
                    "Debes proporcionar el correo y la contraseña."
            );
        }

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(solicitud.getCorreo().trim())
                .orElse(null);
        if (usuario == null) {
            registrarResultado(null, "FALLIDO", "CUENTA_NO_ENCONTRADA");
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        if (usuario.isRestringido()) {

            registrarResultado(usuario.getIdUsuario(), "FALLIDO", "CUENTA_RESTRINGIDA");
            throw new IllegalStateException(
                    "Tu cuenta tiene una restricción administrativa. "
                    + "No puedes iniciar sesión."
            );
        }

        if (usuario.getIntentosFallidos() >= MAX_INTENTOS) {

            registrarResultado(usuario.getIdUsuario(), "FALLIDO", "CUENTA_BLOQUEADA");
            throw new IllegalStateException(
                    "Tu cuenta está bloqueada por intentos fallidos. "
                    + "Debes solicitar su reactivación a un Administrador."
            );
        }

        if (!usuario.isCorreoVerificado()
                || usuario.getEstado() == EstadoUsuario.PENDIENTE_VERIFICACION) {

            registrarResultado(usuario.getIdUsuario(), "FALLIDO", "CORREO_NO_VERIFICADO");
            throw new IllegalStateException(
                    "Debes verificar tu correo antes de iniciar sesión."
            );
        }

        if (usuario.getEstado() == EstadoUsuario.INACTIVA) {

            registrarResultado(usuario.getIdUsuario(), "FALLIDO", "CUENTA_INACTIVA");
            throw new IllegalStateException(
                    "Tu cuenta está desactivada."
            );
        }

        if (usuario.getEstado() != EstadoUsuario.ACTIVA) {

            registrarResultado(usuario.getIdUsuario(), "FALLIDO", "CUENTA_NO_HABILITADA");
            throw new IllegalStateException(
                    "La cuenta no se encuentra habilitada para iniciar sesión."
            );
        }

        if (!proteccionContrasena.verificar(
                solicitud.getContrasena(),
                usuario.getContrasenaHash())) {

            registrarIntentoFallido(usuario);
            registrarResultado(usuario.getIdUsuario(), "FALLIDO", "CONTRASENA_INCORRECTA");

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

            registrarResultado(usuario.getIdUsuario(), "FALLIDO", "ROL_NO_ASIGNADO");
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

        String token = jwtUtil.generateToken(usuario);
        registrarResultado(usuario.getIdUsuario(), "EXITOSO", "INICIO_SESION");
        return token;
    }

    private void registrarResultado(UUID idUsuario, String resultado, String codigo) {
        if (auditoria != null) {
            auditoria.registrarEventoIndependiente(new RegistrarEventoAuditoria(
                    idUsuario,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    "INICIO_SESION",
                    resultado,
                    "USUARIO",
                    idUsuario,
                    "{\"tipo\":\"AUTENTICACION\",\"motivo\":\"" + codigo + "\"}",
                    null));
        }
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
