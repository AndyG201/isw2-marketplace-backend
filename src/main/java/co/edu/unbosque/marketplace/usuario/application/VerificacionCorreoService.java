
package co.edu.unbosque.marketplace.usuario.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.domain.VerificacionCorreo;
import co.edu.unbosque.marketplace.usuario.infrastructure.email.ExternalHTTPRequestMapping;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.VerificacionCorreoRepository;

@Service
public class VerificacionCorreoService {

    private final VerificacionCorreoRepository verificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExternalHTTPRequestMapping correoExterno;
    private final StringRedisTemplate redisTemplate;

    private final SecureRandom random = new SecureRandom();

    // Configuración de reenvíos
    private static final long ESPERA_REENVIO = 60;
    private static final long VENTANA_REENVIO = 60;
    private static final long MAX_REENVIOS = 3;

    public VerificacionCorreoService(
            VerificacionCorreoRepository verificacionRepository,
            UsuarioRepository usuarioRepository,
            ExternalHTTPRequestMapping correoExterno,
            StringRedisTemplate redisTemplate) {

        this.verificacionRepository = verificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.correoExterno = correoExterno;
        this.redisTemplate = redisTemplate;
    }

    // =====================================================
    // ENVÍO INICIAL DEL CÓDIGO
    // HU-001: Registro de usuario
    // =====================================================

    @Transactional
    public void enviarCodigo(String correo) {

        Usuario usuario = obtenerUsuario(correo);

        validarCorreoPendiente(usuario);

        String claveUltimoEnvio = claveUltimoEnvio(correo);

        // El envío inicial establece el tiempo de espera,
        // pero no consume un reenvío.
        redisTemplate.opsForValue().set(
                claveUltimoEnvio,
                "1",
                ESPERA_REENVIO,
                TimeUnit.SECONDS
        );

        try {
            generarYEnviarCodigo(usuario);

        } catch (RuntimeException e) {

            redisTemplate.delete(claveUltimoEnvio);

            throw e;
        }
    }

    // =====================================================
    // REENVÍO DE CÓDIGO
    // Máximo 3 reenvíos por hora
    // =====================================================

    @Transactional
    public void reenviarCodigo(String correo) {

        Usuario usuario = obtenerUsuario(correo);

        validarCorreoPendiente(usuario);

        String claveUltimoEnvio = claveUltimoEnvio(correo);
        String claveContador = claveContadorReenvios(correo);

        // Reservar el intervalo de 60 segundos de forma atómica.
        Boolean puedeReenviar =
                redisTemplate.opsForValue().setIfAbsent(
                        claveUltimoEnvio,
                        "1",
                        ESPERA_REENVIO,
                        TimeUnit.SECONDS
                );

        if (!Boolean.TRUE.equals(puedeReenviar)) {

            Long segundosRestantes = redisTemplate.getExpire(
                    claveUltimoEnvio,
                    TimeUnit.SECONDS
            );

            throw new IllegalStateException(
                    "Debes esperar "
                    + (segundosRestantes != null
                            && segundosRestantes > 0
                            ? segundosRestantes
                            : ESPERA_REENVIO)
                    + " segundos antes de solicitar otro código."
            );
        }

        // Incrementar contador de reenvíos.
        Long reenvios =
                redisTemplate.opsForValue().increment(claveContador);

        if (reenvios == null) {

            redisTemplate.delete(claveUltimoEnvio);

            throw new IllegalStateException(
                    "No se pudo verificar el límite de reenvíos."
            );
        }

        // La ventana de una hora comienza con el primer reenvío.
        if (reenvios == 1) {

            redisTemplate.expire(
                    claveContador,
                    VENTANA_REENVIO,
                    TimeUnit.MINUTES
            );
        }

        // No permitir más de tres reenvíos.
        if (reenvios > MAX_REENVIOS) {

            redisTemplate.opsForValue().decrement(claveContador);

            throw new IllegalStateException(
                    "Has alcanzado el máximo de 3 reenvíos por hora. "
                    + "Intenta nuevamente cuando se restablezca el límite."
            );
        }

        try {

            generarYEnviarCodigo(usuario);

        } catch (RuntimeException e) {

            // Si falla el envío, devolver el cupo utilizado.
            redisTemplate.delete(claveUltimoEnvio);

            Long contadorActual =
                    redisTemplate.opsForValue().decrement(claveContador);

            if (contadorActual != null && contadorActual <= 0) {
                redisTemplate.delete(claveContador);
            }

            throw e;
        }
    }

    // =====================================================
    // GENERAR, INVALIDAR Y ENVIAR CÓDIGO
    // =====================================================

    private void generarYEnviarCodigo(Usuario usuario) {

        String codigo = String.format(
                "%06d",
                random.nextInt(1_000_000)
        );

        OffsetDateTime ahora = OffsetDateTime.now();

        // Invalidar los códigos pendientes anteriores.
        verificacionRepository.invalidarCodigosPendientes(
                usuario,
                ahora
        );

        VerificacionCorreo verificacion =
                new VerificacionCorreo();

        verificacion.setUsuario(usuario);

        verificacion.setCodigoHash(
                calcularHash(codigo)
        );

        // Se conserva el vencimiento actual de 10 minutos.
        verificacion.setFechaExpiracion(
                ahora.plusMinutes(10)
        );

        verificacion.setVerificado(false);

        verificacion.setIntentos(0);

        verificacionRepository.save(verificacion);

        // Enviar el nuevo código.
        correoExterno.enviarCodigoVerificacion(
                usuario.getCorreo(),
                codigo
        );
    }

    // =====================================================
    // OBTENER USUARIO
    // =====================================================

    private Usuario obtenerUsuario(String correo) {

        if (correo == null || correo.isBlank()) {

            throw new IllegalArgumentException(
                    "Debes proporcionar un correo electrónico."
            );
        }

        return usuarioRepository.findByCorreoIgnoreCase(
                correo.trim()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Usuario no encontrado."
                )
        );
    }

    // =====================================================
    // VALIDAR ESTADO DEL CORREO
    // =====================================================

    private void validarCorreoPendiente(Usuario usuario) {

        if (usuario.isCorreoVerificado()) {

            throw new IllegalStateException(
                    "El correo ya está verificado."
            );
        }
    }

    // =====================================================
    // CLAVES REDIS
    // =====================================================

    private String normalizarCorreo(String correo) {

        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String claveUltimoEnvio(String correo) {

        return "verificacion:correo:ultimo-envio:"
                + normalizarCorreo(correo);
    }

    private String claveContadorReenvios(String correo) {

        return "verificacion:correo:contador-reenvios:"
                + normalizarCorreo(correo);
    }

    // =====================================================
    // HASH SHA-256
    // =====================================================

    private String calcularHash(String codigo) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    codigo.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "No se pudo generar el hash.",
                    e
            );
        }
    }

    // =====================================================
    // CONFIRMAR CÓDIGO
    // =====================================================

    @Transactional(
            noRollbackFor = CodigoVerificacionIncorrectoException.class
    )
    public void confirmarCodigo(String correo, String codigo) {

        Usuario usuario = obtenerUsuario(correo);

        if (usuario.isCorreoVerificado()) {

            throw new IllegalStateException(
                    "El correo ya está verificado."
            );
        }

        VerificacionCorreo verificacion =
                verificacionRepository
                        .findTopByUsuarioAndVerificadoFalseOrderByFechaExpiracionDesc(
                                usuario
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No hay un código pendiente de verificación."
                                )
                        );

        OffsetDateTime ahora = OffsetDateTime.now();

        if (!ahora.isBefore(verificacion.getFechaExpiracion())) {

            throw new IllegalStateException(
                    "El código ha expirado."
            );
        }

        if (verificacion.getIntentos() >= 5) {

            throw new IllegalStateException(
                    "Superaste el límite de intentos. Solicita otro código."
            );
        }

        verificacion.setIntentos(
                verificacion.getIntentos() + 1
        );

        String codigoHash = calcularHash(codigo);

        if (!verificacion.getCodigoHash().equals(codigoHash)) {

            verificacionRepository.save(verificacion);

            // Esta excepción no revierte el incremento.
            throw new CodigoVerificacionIncorrectoException(
                    "El código es incorrecto."
            );
        }

        // Activar el código y la cuenta.
        verificacion.setVerificado(true);

        usuario.setCorreoVerificado(true);

        usuario.setFechaActualizacion(ahora);

        verificacionRepository.save(verificacion);

        usuarioRepository.save(usuario);
    }
}
