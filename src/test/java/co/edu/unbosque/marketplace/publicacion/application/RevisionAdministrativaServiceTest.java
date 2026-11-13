package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.RevisionPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.RevisionPublicacionRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RevisionAdministrativaServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RevisionPublicacionRepository revisionRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Usuario administrador;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Usuario anfitrion;

    @Mock
    private Publicacion publicacion;

    @Mock
    private Alojamiento alojamiento;

    @InjectMocks
    private RevisionAdministrativaService service;

    private UUID idAdministrador;
    private UUID idAnfitrion;
    private UUID idPublicacion;
    private UUID idAlojamiento;

    private AtomicReference<String> estadoPublicacion;

    @BeforeEach
    void setUp() {
        idAdministrador = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();

        estadoPublicacion = new AtomicReference<>("PENDIENTE_REVISION");

        when(administrador.getIdUsuario()).thenReturn(idAdministrador);
        when(administrador.getRol().getNombre()).thenReturn("ADMINISTRADOR");

        when(anfitrion.getIdUsuario()).thenReturn(idAnfitrion);
        when(anfitrion.getEstado()).thenReturn(EstadoUsuario.ACTIVA);
        when(anfitrion.isRestringido()).thenReturn(false);
        when(anfitrion.isCorreoVerificado()).thenReturn(true);
        when(anfitrion.getIntentosFallidos()).thenReturn(0);
        when(anfitrion.getRol().getNombre()).thenReturn("ANFITRION");

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getEstado()).thenAnswer(
                invocation -> estadoPublicacion.get());

        doAnswer(invocation -> {
            estadoPublicacion.set(invocation.getArgument(0));
            return null;
        }).when(publicacion).setEstado(any());

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(revisionRepository.save(any(RevisionPublicacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void prepararAlojamientoYAnfitrion() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(alojamiento.getIdAnfitrion()).thenReturn(idAnfitrion);

        when(usuarioRepository.findById(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));
    }

    @Test
    void aprobarPublicacionCorrectamente() {
        prepararAlojamientoYAnfitrion();

        var respuesta = service.aprobarPublicacion(
                administrador, idPublicacion);

        assertNotNull(respuesta);
        assertTrue("ACTIVA".equals(estadoPublicacion.get()));

        verify(publicacion).setEstado("ACTIVA");
        verify(publicacion).setFechaActualizacion(any());
        verify(publicacionRepository).save(publicacion);
        verify(revisionRepository).save(any(RevisionPublicacion.class));
    }

    @Test
    void rechazarPublicacionCorrectamente() {
        var respuesta = service.rechazarPublicacion(
                administrador, idPublicacion, "Información incompleta");

        assertNotNull(respuesta);
        assertTrue("BORRADOR".equals(estadoPublicacion.get()));

        verify(publicacion).setEstado("BORRADOR");
        verify(publicacion).setFechaActualizacion(any());
        verify(publicacionRepository).save(publicacion);
        verify(revisionRepository).save(any(RevisionPublicacion.class));
        verifyNoInteractions(alojamientoRepository, usuarioRepository);
    }

    @Test
    void aprobarConAdministradorNulo() {
        assertThrows(AccessDeniedException.class,
                () -> service.aprobarPublicacion(null, idPublicacion));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConAdministradorNulo() {
        assertThrows(AccessDeniedException.class,
                () -> service.rechazarPublicacion(
                        null, idPublicacion, "Motivo"));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void aprobarConIdentificadorDeAdministradorNulo() {
        when(administrador.getIdUsuario()).thenReturn(null);

        assertThrows(AccessDeniedException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConRolIncorrecto() {
        when(administrador.getRol().getNombre()).thenReturn("ANFITRION");

        assertThrows(AccessDeniedException.class,
                () -> service.rechazarPublicacion(
                        administrador, idPublicacion, "Motivo"));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void aprobarConRolNulo() {
        when(administrador.getRol()).thenReturn(null);

        assertThrows(AccessDeniedException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void aprobarConIdentificadorDePublicacionNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.aprobarPublicacion(
                        administrador, null));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConIdentificadorDePublicacionNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.rechazarPublicacion(
                        administrador, null, "Motivo"));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void aprobarPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verifyNoInteractions(alojamientoRepository, usuarioRepository);
    }

    @Test
    void rechazarPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> service.rechazarPublicacion(
                        administrador, idPublicacion, "Motivo"));
    }

    @Test
    void aprobarPublicacionEnEstadoIncorrecto() {
        estadoPublicacion.set("BORRADOR");

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verifyNoInteractions(alojamientoRepository, usuarioRepository);
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void rechazarPublicacionEnEstadoIncorrecto() {
        estadoPublicacion.set("ACTIVA");

        assertThrows(IllegalStateException.class,
                () -> service.rechazarPublicacion(
                        administrador, idPublicacion, "Motivo"));

        verify(publicacionRepository, never()).save(any());
        verifyNoInteractions(revisionRepository);
    }

    @Test
    void aprobarSinAlojamientoAsociado() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verifyNoInteractions(usuarioRepository);
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarSinCuentaDeAnfitrion() {
        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(alojamiento.getIdAnfitrion()).thenReturn(idAnfitrion);

        when(usuarioRepository.findById(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConAnfitrionInactivo() {
        prepararAlojamientoYAnfitrion();
        when(anfitrion.getEstado()).thenReturn(EstadoUsuario.INACTIVA);

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConAnfitrionRestringido() {
        prepararAlojamientoYAnfitrion();
        when(anfitrion.isRestringido()).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConCorreoDeAnfitrionNoVerificado() {
        prepararAlojamientoYAnfitrion();
        when(anfitrion.isCorreoVerificado()).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConCincoIntentosFallidosDelAnfitrion() {
        prepararAlojamientoYAnfitrion();
        when(anfitrion.getIntentosFallidos()).thenReturn(5);

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void aprobarConRolIncorrectoDelAnfitrion() {
        prepararAlojamientoYAnfitrion();
        when(anfitrion.getRol().getNombre()).thenReturn("CLIENTE");

        assertThrows(IllegalStateException.class,
                () -> service.aprobarPublicacion(
                        administrador, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void rechazarConMotivoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.rechazarPublicacion(
                        administrador, idPublicacion, null));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConMotivoVacio() {
        assertThrows(IllegalArgumentException.class,
                () -> service.rechazarPublicacion(
                        administrador, idPublicacion, "   "));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConMotivoMayorA2000Caracteres() {
        String motivo = "A".repeat(2001);

        assertThrows(IllegalArgumentException.class,
                () -> service.rechazarPublicacion(
                        administrador, idPublicacion, motivo));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void rechazarConMotivoDe2000Caracteres() {
        String motivo = "A".repeat(2000);

        var respuesta = service.rechazarPublicacion(
                administrador, idPublicacion, motivo);

        assertNotNull(respuesta);
        assertTrue("BORRADOR".equals(estadoPublicacion.get()));

        verify(revisionRepository).save(any(RevisionPublicacion.class));
    }
}
