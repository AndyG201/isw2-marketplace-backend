package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Answers;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionEstadoServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Usuario anfitrion;

    @Mock
    private Publicacion publicacion;

    @Mock
    private Alojamiento alojamiento;

    @InjectMocks
    private PublicacionEstadoService service;

    private UUID idUsuario;
    private UUID idPublicacion;
    private UUID idAlojamiento;

    @BeforeEach
    void setUp() {
        idUsuario = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();

        when(anfitrion.getIdUsuario()).thenReturn(idUsuario);
        when(anfitrion.getRol().getNombre()).thenReturn("ANFITRION");
        when(anfitrion.isCorreoVerificado()).thenReturn(true);
        when(anfitrion.getEstado()).thenReturn(EstadoUsuario.ACTIVA);
        when(anfitrion.isRestringido()).thenReturn(false);
        when(anfitrion.getIntentosFallidos()).thenReturn(0);

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);

        when(alojamiento.getIdAnfitrion()).thenReturn(idUsuario);
    }

    private void prepararPublicacion(String estado) {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(publicacion.getEstado()).thenReturn(estado);
    }

    @Test
    void pausarPublicacionCorrectamente() {
        prepararPublicacion("ACTIVA");

        assertDoesNotThrow(() ->
                service.pausarPublicacion(anfitrion, idPublicacion));

        verify(publicacion).setEstado("PAUSADA");
        verify(publicacion).setFechaActualizacion(any());
        verify(publicacionRepository).save(publicacion);
    }

    @Test
    void reactivarPublicacionCorrectamente() {
        prepararPublicacion("PAUSADA");

        assertDoesNotThrow(() ->
                service.reactivarPublicacion(anfitrion, idPublicacion));

        verify(publicacion).setEstado("ACTIVA");
        verify(publicacion).setFechaActualizacion(any());
        verify(publicacionRepository).save(publicacion);
    }

    @Test
    void pausarConAnfitrionNulo() {
        assertThrows(AccessDeniedException.class,
                () -> service.pausarPublicacion(null, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void reactivarConAnfitrionSinIdentificador() {
        when(anfitrion.getIdUsuario()).thenReturn(null);

        assertThrows(AccessDeniedException.class,
                () -> service.reactivarPublicacion(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void pausarConRolDiferenteDeAnfitrion() {
        when(anfitrion.getRol().getNombre()).thenReturn("CLIENTE");

        assertThrows(AccessDeniedException.class,
                () -> service.pausarPublicacion(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void reactivarConCorreoNoVerificado() {
        when(anfitrion.isCorreoVerificado()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.reactivarPublicacion(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void pausarConCuentaRestringida() {
        when(anfitrion.isRestringido()).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> service.pausarPublicacion(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void reactivarConCincoIntentosFallidos() {
        when(anfitrion.getIntentosFallidos()).thenReturn(5);

        assertThrows(AccessDeniedException.class,
                () -> service.reactivarPublicacion(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void pausarConIdentificadorNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.pausarPublicacion(anfitrion, null));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void reactivarPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> service.reactivarPublicacion(anfitrion, idPublicacion));

        verifyNoInteractions(alojamientoRepository);
    }

    @Test
    void pausarSinAlojamientoAsociado() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.pausarPublicacion(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarPublicacionDeOtroAnfitrion() {
        UUID otroAnfitrion = UUID.randomUUID();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(alojamiento.getIdAnfitrion()).thenReturn(otroAnfitrion);

        assertThrows(AccessDeniedException.class,
                () -> service.pausarPublicacion(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void reactivarPublicacionDeOtroAnfitrion() {
        UUID otroAnfitrion = UUID.randomUUID();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(alojamiento.getIdAnfitrion()).thenReturn(otroAnfitrion);

        assertThrows(AccessDeniedException.class,
                () -> service.reactivarPublicacion(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarPublicacionBloqueada() {
        prepararPublicacion("BLOQUEADA");

        assertThrows(IllegalStateException.class,
                () -> service.pausarPublicacion(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void reactivarPublicacionBloqueada() {
        prepararPublicacion("BLOQUEADA");

        assertThrows(IllegalStateException.class,
                () -> service.reactivarPublicacion(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarPublicacionQueNoEstaActiva() {
        prepararPublicacion("PAUSADA");

        assertThrows(IllegalStateException.class,
                () -> service.pausarPublicacion(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void reactivarPublicacionQueNoEstaPausada() {
        prepararPublicacion("ACTIVA");

        assertThrows(IllegalStateException.class,
                () -> service.reactivarPublicacion(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void pausarRetornaRespuesta() {
        prepararPublicacion("ACTIVA");

        var respuesta = service.pausarPublicacion(anfitrion, idPublicacion);

        assertNotNull(respuesta);
    }

    @Test
    void reactivarRetornaRespuesta() {
        prepararPublicacion("PAUSADA");

        var respuesta = service.reactivarPublicacion(anfitrion, idPublicacion);

        assertNotNull(respuesta);
    }
}
