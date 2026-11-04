package co.edu.unbosque.marketplace.usuario.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.usuario.api.request.RegistrarUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.email.ExternalHTTPRequestMapping;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.RolRepository;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.security.ProteccionContrasena;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ProteccionDocumento proteccionDocumento;

    @Autowired
    private ProteccionContrasena proteccionContrasena;

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ExternalHTTPRequestMapping correoExterno;

    private final SecureRandom random = new SecureRandom();

    public int registrar(RegistrarUsuarioRequest nuevoUsuario) {

        if (datosInvalidos(nuevoUsuario)) {
            return 0;
        }

        Optional<Rol> rolEncontrado =
                buscarRolPermitido(nuevoUsuario.getRol());

        if (rolEncontrado.isEmpty()) {
            return 0;
        }

        if (correoExiste(nuevoUsuario.getCorreo())) {
            return 0;
        }

        if (telefonoExiste(nuevoUsuario.getTelefono())) {
            return 0;
        }

        ProteccionDocumento.DatosDocumento datosDocumento =
                proteccionDocumento.protegerDocumento(
                        nuevoUsuario.getTipoDocumento(),
                        nuevoUsuario.getDocumentoIdentidad()
                );

        if (documentoExiste(datosDocumento.documentoHash())) {
            return 0;
        }

        Usuario usuario = crearUsuario(
                nuevoUsuario,
                datosDocumento,
                rolEncontrado.get()
        );

        completarCampos(usuario);

        usuarioRepository.save(usuario);

        verificacionCorreoService.enviarCodigo(usuario.getCorreo());

        return 1;
    }

    private Optional<Rol> buscarRolPermitido(String nombreRol) {

        if (textoVacio(nombreRol)) {
            return Optional.empty();
        }

        String rolNormalizado =
                nombreRol.trim().toUpperCase(Locale.ROOT);

        if (!rolNormalizado.equals("HUESPED")
                && !rolNormalizado.equals("ANFITRION")) {
            return Optional.empty();
        }

        return rolRepository.findByNombreIgnoreCase(rolNormalizado);
    }

    private boolean datosInvalidos(RegistrarUsuarioRequest datos) {

        return datos == null
                || textoVacio(datos.getNombre())
                || textoVacio(datos.getApellido())
                || textoVacio(datos.getCorreo())
                || textoVacio(datos.getTipoDocumento())
                || textoVacio(datos.getDocumentoIdentidad())
                || textoVacio(datos.getTelefono())
                || textoVacio(datos.getContrasena())
                || textoVacio(datos.getRol());
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean correoExiste(String correo) {

        return usuarioRepository
                .findByCorreoIgnoreCase(correo.trim())
                .isPresent();
    }

    private boolean documentoExiste(byte[] documentoHash) {

        return usuarioRepository.existsByDocumentoHash(documentoHash);
    }

    private boolean telefonoExiste(String telefono) {

        return usuarioRepository.existsByTelefono(telefono.trim());
    }

    private Usuario crearUsuario(
            RegistrarUsuarioRequest datos,
            ProteccionDocumento.DatosDocumento datosDocumento,
            Rol rol) {

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setCorreo(datos.getCorreo().trim());
        usuario.setTipoDocumento(datos.getTipoDocumento());
        usuario.setTelefono(datos.getTelefono().trim());

        usuario.setRol(rol);

        usuario.setDocumentoCifrado(datosDocumento.documentoCifrado());
        usuario.setDocumentoNonce(datosDocumento.documentoNonce());
        usuario.setDocumentoHash(datosDocumento.documentoHash());

        usuario.setContrasenaHash(
                proteccionContrasena.generarHash(datos.getContrasena())
        );

        return usuario;
    }

    private void completarCampos(Usuario usuario) {

        OffsetDateTime ahora = OffsetDateTime.now();

        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setCorreoVerificado(false);
        usuario.setEstado(EstadoUsuario.PENDIENTE_VERIFICACION);
        usuario.setRestringido(false);
        usuario.setIntentosFallidos(0);
        usuario.setMotivoRestriccion(null);
        usuario.setFechaRegistro(ahora);
        usuario.setFechaActualizacion(ahora);
    }

    public boolean generarCodigoRecuperacion(String correo) {

        if (correo == null || correo.isBlank()) {
            return false;
        }

        Optional<Usuario> resultado =
                usuarioRepository.findByCorreoIgnoreCase(correo.trim());

        if (resultado.isEmpty()) {
            return false;
        }

        String correoUsuario = resultado.get().getCorreo();

        String codigo = String.format(
                "%06d",
                random.nextInt(1_000_000)
        );

        String clave = "recuperacion:contrasena:"
                + correoUsuario.toLowerCase(Locale.ROOT);

        redisTemplate.opsForValue().set(
                clave,
                calcularHashCodigo(codigo),
                5,
                TimeUnit.MINUTES
        );

        try {
            correoExterno.enviarCodigoRecuperacion(correoUsuario, codigo);
            return true;

        } catch (Exception e) {
            redisTemplate.delete(clave);
            throw e;
        }
    }

    private String calcularHashCodigo(String codigo) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    codigo.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo generar el hash del código",
                    e
            );
        }
    }

    public boolean validarCodigoRecuperacion(
            String correo,
            String codigo) {

        if (correo == null || correo.isBlank()
                || codigo == null
                || !codigo.matches("\\d{6}")) {
            return false;
        }

        String correoNormalizado =
                correo.trim().toLowerCase(Locale.ROOT);

        String claveCodigo =
                "recuperacion:contrasena:" + correoNormalizado;

        String claveIntentos =
                "recuperacion:contrasena:intentos:" + correoNormalizado;

        String hashGuardado =
                redisTemplate.opsForValue().get(claveCodigo);

        if (hashGuardado == null) {
            return false;
        }

        String hashIngresado = calcularHashCodigo(codigo);

        if (hashGuardado.equals(hashIngresado)) {
            return true;
        }

        Long intentos =
                redisTemplate.opsForValue().increment(claveIntentos);

        if (intentos != null && intentos == 1) {
            redisTemplate.expire(
                    claveIntentos,
                    5,
                    TimeUnit.MINUTES
            );
        }

        if (intentos != null && intentos >= 5) {
            redisTemplate.delete(claveCodigo);
            redisTemplate.delete(claveIntentos);
        }

        return false;
    }

    public boolean restablecerContrasena(
            String correo,
            String codigo,
            String nuevaContrasena) {

        if (correo == null || correo.isBlank()
                || nuevaContrasena == null
                || nuevaContrasena.isBlank()) {
            return false;
        }

        if (!validarCodigoRecuperacion(correo, codigo)) {
            return false;
        }

        Optional<Usuario> resultado =
                usuarioRepository.findByCorreoIgnoreCase(correo.trim());

        if (resultado.isEmpty()) {
            return false;
        }

        Usuario usuario = resultado.get();

        usuario.setContrasenaHash(
                proteccionContrasena.generarHash(nuevaContrasena)
        );

        usuario.setFechaActualizacion(OffsetDateTime.now());

        usuarioRepository.save(usuario);

        String correoNormalizado =
                usuario.getCorreo().toLowerCase(Locale.ROOT);

        redisTemplate.delete(
                "recuperacion:contrasena:" + correoNormalizado
        );

        redisTemplate.delete(
                "recuperacion:contrasena:intentos:" + correoNormalizado
        );

        return true;
    }
}