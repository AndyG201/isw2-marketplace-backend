package co.edu.unbosque.marketplace.usuario.application;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.usuario.application.dto.ModificarDocumentoRequest;
import co.edu.unbosque.marketplace.usuario.application.port.UsuarioRepository;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;

@ExtendWith(MockitoExtension.class)
class AdministradorDocumentoServiceTest {

    private static final byte[] HASH_ACTUAL = new byte[32];
    private static final byte[] HASH_NUEVO = new byte[32];

    static {
        HASH_NUEVO[0] = 7;
    }

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProteccionDocumento proteccionDocumento;

    @Mock
    private AuditoriaEventoCommand auditoria;

    private AdministradorService service;
    private final UUID idUsuario = UUID.randomUUID();
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        service = new AdministradorService(usuarioRepository, jwtUtil, eventPublisher);
        service.configurarAuditoria(auditoria);
        service.configurarProteccionDocumento(proteccionDocumento);
        usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setTipoDocumento("CC");
        usuario.setDocumentoHash(HASH_ACTUAL);
    }

    private ProteccionDocumento.DatosDocumento datos(byte[] hash) {
        return new ProteccionDocumento.DatosDocumento(new byte[] { 1, 2 }, new byte[12], hash);
    }

    @Test
    void cambiaElTipoYElNumeroYRegistraLaAccionSinGuardarElNumero() {
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(proteccionDocumento.protegerDocumento("CE", " AB12345 ")).thenReturn(datos(HASH_NUEVO));
        when(usuarioRepository.existsByDocumentoHash(HASH_NUEVO)).thenReturn(false);

        service.modificarDocumento(idUsuario, new ModificarDocumentoRequest(" ce ", " AB12345 "));

        assertEquals("CE", usuario.getTipoDocumento());
        assertArrayEquals(HASH_NUEVO, usuario.getDocumentoHash());
        assertArrayEquals(new byte[] { 1, 2 }, usuario.getDocumentoCifrado());
        assertNotNull(usuario.getFechaActualizacion());
        verify(usuarioRepository).save(usuario);

        ArgumentCaptor<RegistrarEventoAuditoria> evento =
                ArgumentCaptor.forClass(RegistrarEventoAuditoria.class);
        verify(auditoria).registrarEvento(evento.capture());
        assertEquals("MODIFICAR_DOCUMENTO_USUARIO", evento.getValue().accion());
        assertEquals("USUARIO", evento.getValue().entidadAfectada());
        assertEquals(idUsuario, evento.getValue().idEntidadAfectada());
        assertEquals("{\"campo\":\"DOCUMENTO\",\"tipoAnterior\":\"CC\",\"tipoNuevo\":\"CE\"}",
                evento.getValue().detalle());
        assertFalse(evento.getValue().detalle().contains("AB12345"));
    }

    @Test
    void rechazaUnDocumentoQueYaTieneOtraCuenta() {
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(proteccionDocumento.protegerDocumento("CC", "99999")).thenReturn(datos(HASH_NUEVO));
        when(usuarioRepository.existsByDocumentoHash(HASH_NUEVO)).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.modificarDocumento(idUsuario, new ModificarDocumentoRequest("CC", "99999")));

        assertEquals("Ya existe otra cuenta con ese documento.", error.getMessage());
        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(auditoria);
    }

    @Test
    void avisaSiElDocumentoEsElMismoQueYaTiene() {
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(proteccionDocumento.protegerDocumento("CC", "12345")).thenReturn(datos(HASH_ACTUAL));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.modificarDocumento(idUsuario, new ModificarDocumentoRequest("CC", "12345")));

        assertEquals("La cuenta ya tiene registrado ese documento.", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaTiposDesconocidosYUsuariosQueNoExisten() {
        IllegalArgumentException tipo = assertThrows(IllegalArgumentException.class,
                () -> service.modificarDocumento(idUsuario, new ModificarDocumentoRequest("NIT", "12345")));
        assertEquals("El tipo de documento debe ser CC, CE, TI o PASAPORTE.", tipo.getMessage());

        IllegalArgumentException vacio = assertThrows(IllegalArgumentException.class,
                () -> service.modificarDocumento(idUsuario, new ModificarDocumentoRequest("CC", " ")));
        assertEquals("Debes proporcionar el tipo y el número de documento.", vacio.getMessage());

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());
        IllegalArgumentException inexistente = assertThrows(IllegalArgumentException.class,
                () -> service.modificarDocumento(idUsuario, new ModificarDocumentoRequest("CC", "12345")));
        assertEquals("Usuario no encontrado.", inexistente.getMessage());
        verifyNoInteractions(proteccionDocumento);
    }
}
