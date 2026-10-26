
package co.edu.unbosque.marketplace.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.model.Usuario;
import co.edu.unbosque.marketplace.model.emun.EstadoUsuario;
import co.edu.unbosque.marketplace.repository.UsuarioRepository;
import co.edu.unbosque.marketplace.request.RegistrarUsuarioRequest;
import co.edu.unbosque.marketplace.security.ProteccionDocumento;
import co.edu.unbosque.marketplace.security.ProteccionContrasena;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProteccionDocumento proteccionDocumento;

    @Autowired
    private ProteccionContrasena proteccionContrasena;
    
    @Autowired
    private VerificacionCorreoService verificacionCorreoService;
    
    


    public int registrar(RegistrarUsuarioRequest nuevoUsuario) {

        if (datosInvalidos(nuevoUsuario)) {
            return 0;
        }

        if (correoExiste(nuevoUsuario.getCorreo())) {
            return 0;
        }

        if (telefonoExiste(nuevoUsuario.getTelefono())) {
            return 0;
        }

        ProteccionDocumento.DatosDocumento datosDocumento =proteccionDocumento.protegerDocumento(nuevoUsuario.getTipoDocumento(),nuevoUsuario.getDocumentoIdentidad());

        if (documentoExiste(datosDocumento.documentoHash())) {
            return 0;
        }
        Usuario usuario = crearUsuario(nuevoUsuario, datosDocumento);
        completarCampos(usuario);

        usuarioRepository.save(usuario);

        verificacionCorreoService.enviarCodigo(usuario.getCorreo());

        return 1;
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
                || datos.getIdRol() == null;
    }


    private boolean textoVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private boolean correoExiste(String correo) {

        Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);
        return usuario.isPresent();
    }

    private boolean documentoExiste(byte[] documentoHash) {

        return usuarioRepository.existsByDocumentoHash(documentoHash);
    }


    private boolean telefonoExiste(String telefono) {

        return usuarioRepository.existsByTelefono(telefono);
    }


    private Usuario crearUsuario(
        RegistrarUsuarioRequest datos, ProteccionDocumento.DatosDocumento datosDocumento) {

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setCorreo(datos.getCorreo());
        usuario.setTipoDocumento(datos.getTipoDocumento());
        usuario.setTelefono(datos.getTelefono());
        usuario.setIdRol(datos.getIdRol());

        usuario.setDocumentoCifrado(datosDocumento.documentoCifrado());

        usuario.setDocumentoNonce(datosDocumento.documentoNonce());

        usuario.setDocumentoHash(datosDocumento.documentoHash());

        usuario.setContrasenaHash(proteccionContrasena.generarHash(datos.getContrasena()));

        return usuario;
    }


    private void completarCampos(Usuario usuario) {

        OffsetDateTime ahora = OffsetDateTime.now();

        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setCorreoVerificado(false);
        usuario.setEstado(
                EstadoUsuario.PENDIENTE_VERIFICACION);
        usuario.setRestringido(false);
        usuario.setIntentosFallidos(0);
        usuario.setMotivoRestriccion(null);
        usuario.setFechaRegistro(ahora);
        usuario.setFechaActualizacion(ahora);
    }
}
