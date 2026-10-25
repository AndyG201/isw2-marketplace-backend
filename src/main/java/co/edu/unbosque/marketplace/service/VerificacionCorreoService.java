
package co.edu.unbosque.marketplace.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unbosque.marketplace.model.Usuario;
import co.edu.unbosque.marketplace.model.VerificacionCorreo;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.repository.VerificacionCorreoRepository;

@Service
public class VerificacionCorreoService {

    private final VerificacionCorreoRepository verificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExternalHTTPRequestMapping correoExterno;

    private final SecureRandom random = new SecureRandom();

    public VerificacionCorreoService(
            VerificacionCorreoRepository verificacionRepository,
            UsuarioRepository usuarioRepository,
            ExternalHTTPRequestMapping correoExterno) {
        this.verificacionRepository = verificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.correoExterno = correoExterno;
    }


    @Transactional
    public void enviarCodigo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.isCorreoVerificado()) {
            throw new IllegalStateException("El correo ya esta verificado");
        }

        String codigo = String.format("%06d", random.nextInt(1_000_000));

        VerificacionCorreo verificacion = new VerificacionCorreo();
        verificacion.setUsuario(usuario);
        verificacion.setCodigoHash(calcularHash(codigo));
        verificacion.setFechaExpiracion(OffsetDateTime.now().plusMinutes(10));
        verificacion.setVerificado(false);
        verificacion.setIntentos(0);

        verificacionRepository.save(verificacion);

        correoExterno.enviarCodigoVerificacion(correo, codigo);
    }

    private String calcularHash(String codigo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codigo.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el hash", e);
        }
    }
    
    @Transactional
    public void confirmarCodigo(String correo, String codigo) {

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElseThrow(() ->new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.isCorreoVerificado()) {
            throw new IllegalStateException("El correo ya esta verificado");
        }

        VerificacionCorreo verificacion =
                verificacionRepository
                    .findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(usuario)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No hay un codigo pendiente de verificacion"));

        if (OffsetDateTime.now().isAfter(verificacion.getFechaExpiracion())) {
            throw new IllegalStateException("El codigo ha expirado");
        }

        if (verificacion.getIntentos() >= 5) {
            throw new IllegalStateException(
                    "Superaste el limite de intentos. Solicita otro codigo");
        }

        verificacion.setIntentos(verificacion.getIntentos() + 1);

        String codigoHash = calcularHash(codigo);

        if (!verificacion.getCodigoHash().equals(codigoHash)) {
            verificacionRepository.save(verificacion);
            throw new IllegalArgumentException("El codigo es incorrecto");
        }

        verificacion.setVerificado(true);
        usuario.setCorreoVerificado(true);
        usuario.setFechaActualizacion(OffsetDateTime.now());

        verificacionRepository.save(verificacion);
        usuarioRepository.save(usuario);
    }
}
