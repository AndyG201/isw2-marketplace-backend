package co.edu.unbosque.marketplace.usuario.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.ubicacion.contracts.UbicacionRegistroQuery;
import co.edu.unbosque.marketplace.usuario.application.dto.ActualizarContrasenaRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.ActualizarPerfilRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.ReactivarCuentaRequest;
import co.edu.unbosque.marketplace.usuario.application.port.RolRepository;
import co.edu.unbosque.marketplace.usuario.application.port.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.ServicioCorreo;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.ProteccionContrasena;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;

/** Ramas de error de UsuarioService: cada una debe lanzar la excepción de dominio que ErroresApiHandler traduce a HTTP. */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceExcepcionesTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String CORREO = "ana@ejemplo.com";
    private static final String PREFIJO = "perfil:cambio-correo:" + ID;
    // SHA-256 del código 123456.
    private static final String HASH_CODIGO = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private ProteccionDocumento proteccionDocumento;
    @Mock private ProteccionContrasena proteccionContrasena;
    @Mock private VerificacionCorreoService verificacionCorreoService;
    @Mock private ServicioCorreo servicioCorreo;
    @Mock private JwtUtil jwtUtil;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private UbicacionRegistroQuery ubicacionRegistroQuery;
    @Mock private AlmacenTemporal almacenTemporal;
    @InjectMocks private UsuarioService service;

    private Usuario usuario(EstadoUsuario estado, boolean correoVerificado) {
        Usuario u = new Usuario();
        u.setIdUsuario(ID);
        u.setCorreo(CORREO);
        u.setContrasenaHash("hash");
        u.setEstado(estado);
        u.setCorreoVerificado(correoVerificado);
        return u;
    }

    private ReactivarCuentaRequest credenciales(String correo, String contrasena) {
        ReactivarCuentaRequest r = new ReactivarCuentaRequest();
        r.setCorreo(correo);
        r.setContrasena(contrasena);
        return r;
    }

    @Test
    void consultarMiCuentaExigeUsuarioIdentificado() {
        assertThrows(SesionRequeridaException.class, () -> service.consultarMiCuenta(null));
    }

    @Test
    void actualizarPerfilExigeTodosLosDatos() {
        assertThrows(DatosInvalidosException.class, () -> service.actualizarPerfil(ID, new ActualizarPerfilRequest()));
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void confirmarCambioCorreoExigeUnCodigoDeSeisDigitos() {
        assertThrows(DatosInvalidosException.class, () -> service.confirmarCambioCorreo(ID, "12"));
        assertThrows(DatosInvalidosException.class, () -> service.confirmarCambioCorreo(null, "123456"));
    }

    @Test
    void confirmarCambioCorreoRechazaUnCorreoQueYaPerteneceAOtraCuenta() {
        when(usuarioRepository.findById(ID)).thenReturn(Optional.of(usuario(EstadoUsuario.ACTIVA, true)));
        when(almacenTemporal.obtener(PREFIJO + ":correo")).thenReturn("otro@ejemplo.com");
        when(almacenTemporal.obtener(PREFIJO + ":codigo")).thenReturn(HASH_CODIGO);
        when(usuarioRepository.existsByCorreoIgnoreCaseAndIdUsuarioNot("otro@ejemplo.com", ID)).thenReturn(true);

        assertThrows(ConflictoNegocioException.class, () -> service.confirmarCambioCorreo(ID, "123456"));
    }

    @Test
    void actualizarContrasenaExigeLasDosContrasenas() {
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarContrasena(ID, new ActualizarContrasenaRequest()));
    }

    @Test
    void desactivarCuentaExigeUsuarioIdentificado() {
        assertThrows(DatosInvalidosException.class, () -> service.desactivarCuenta(null));
    }

    @Test
    void desactivarCuentaRechazaUnaCuentaQueNoEstaActiva() {
        when(usuarioRepository.findById(ID)).thenReturn(Optional.of(usuario(EstadoUsuario.INACTIVA, true)));

        assertThrows(ConflictoNegocioException.class, () -> service.desactivarCuenta(ID));
    }

    @Test
    void reactivarCuentaExigeCorreoYContrasena() {
        assertThrows(SesionRequeridaException.class, () -> service.reactivarCuenta(null));
        assertThrows(SesionRequeridaException.class, () -> service.reactivarCuenta(credenciales(CORREO, " ")));
    }

    @Test
    void reactivarCuentaRechazaCuentasRestringidasOBloqueadas() {
        Usuario restringido = usuario(EstadoUsuario.INACTIVA, true);
        restringido.setRestringido(true);
        Usuario bloqueado = usuario(EstadoUsuario.INACTIVA, true);
        bloqueado.setIntentosFallidos(5);
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(Optional.of(restringido), Optional.of(bloqueado));

        assertThrows(AccesoNoPermitidoException.class, () -> service.reactivarCuenta(credenciales(CORREO, "Clave123")));
        assertThrows(AccesoNoPermitidoException.class, () -> service.reactivarCuenta(credenciales(CORREO, "Clave123")));
    }

    @Test
    void reactivarCuentaRechazaCuentasQueNoEstanDesactivadasONoVerificadas() {
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO)).thenReturn(
                Optional.of(usuario(EstadoUsuario.ACTIVA, true)),
                Optional.of(usuario(EstadoUsuario.INACTIVA, false)));

        assertThrows(AccesoNoPermitidoException.class, () -> service.reactivarCuenta(credenciales(CORREO, "Clave123")));
        assertThrows(AccesoNoPermitidoException.class, () -> service.reactivarCuenta(credenciales(CORREO, "Clave123")));
    }
}
