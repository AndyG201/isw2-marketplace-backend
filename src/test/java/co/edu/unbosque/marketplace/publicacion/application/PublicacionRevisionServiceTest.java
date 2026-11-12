package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.BandejaSalidaAuditoriaRepository;
import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.publicacion.api.response.EnviarRevisionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.servicio.domain.Servicio;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionRevisionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @Mock
    private ImagenPublicacionRepository imagenRepository;

    @Mock
    private PublicacionServicioRepository publicacionServicioRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private PoliticaCancelacionRepository politicaRepository;

    @Mock
    private BandejaSalidaAuditoriaRepository auditoriaRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Usuario anfitrion;

    @Mock
    private Publicacion publicacion;

    @Mock
    private Alojamiento alojamiento;

    @InjectMocks
    private PublicacionRevisionService service;

    private UUID idUsuario;
    private UUID idPublicacion;
    private UUID idAlojamiento;
    private UUID idPolitica;
    private UUID idServicio;

    @BeforeEach
    void setUp() {
        idUsuario = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        idPolitica = UUID.randomUUID();
        idServicio = UUID.randomUUID();

        when(anfitrion.getIdUsuario()).thenReturn(idUsuario);
        when(anfitrion.getRol().getNombre()).thenReturn("ANFITRION");
        when(anfitrion.isCorreoVerificado()).thenReturn(true);
        when(anfitrion.getEstado()).thenReturn(EstadoUsuario.ACTIVA);
        when(anfitrion.isRestringido()).thenReturn(false);
        when(anfitrion.getIntentosFallidos()).thenReturn(0);

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getEstado()).thenReturn("BORRADOR");

        when(alojamiento.getIdAnfitrion()).thenReturn(idUsuario);
    }

    private void prepararDatosValidos() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(publicacion.getTitulo()).thenReturn("Apartamento");
        when(publicacion.getDescripcion()).thenReturn("Apartamento amoblado");
        when(publicacion.getReglas()).thenReturn("No fumar");

        when(alojamiento.getUbicacion()).thenReturn("Centro");
        when(alojamiento.getCiudad()).thenReturn("Bogotá");
        when(alojamiento.getTipoAlojamiento()).thenReturn("APARTAMENTO");
        when(alojamiento.getCapacidad()).thenReturn(4);

        when(publicacion.getPrecioPorNoche())
                .thenReturn(new BigDecimal("150000"));

        when(publicacion.getTarifaLimpieza())
                .thenReturn(new BigDecimal("20000"));

        when(publicacion.getModoAceptacion()).thenReturn("INMEDIATA");
        when(publicacion.getIdPolitica()).thenReturn(idPolitica);

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(mock(PoliticaCancelacion.class)));

        PublicacionServicio relacion = mock(PublicacionServicio.class);
        when(relacion.getIdServicio()).thenReturn(idServicio);

        when(publicacionServicioRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(relacion));

        when(servicioRepository.findAllById(List.of(idServicio)))
                .thenReturn(List.of(mock(Servicio.class)));

        ImagenPublicacion imagen = mock(ImagenPublicacion.class);
        when(imagen.getRutaImagen()).thenReturn("imagenes/apartamento.jpg");

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of(imagen));
    }

    @Test
    void enviarRevisionCorrectamente() {
        prepararDatosValidos();

        EnviarRevisionResponse respuesta =
                service.enviarRevision(anfitrion, idPublicacion);

        assertNotNull(respuesta);

        verify(publicacion).setEstado("PENDIENTE_REVISION");
        verify(publicacion).setFechaActualizacion(any());
        verify(publicacionRepository).save(publicacion);
        verify(auditoriaRepository).save(any());
    }

    @Test
    void enviarRevisionConAnfitrionNulo() {
        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(null, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void enviarRevisionConIdentificadorDeAnfitrionNulo() {
        when(anfitrion.getIdUsuario()).thenReturn(null);

        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void enviarRevisionConRolIncorrecto() {
        when(anfitrion.getRol().getNombre()).thenReturn("CLIENTE");

        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository, alojamientoRepository);
    }

    @Test
    void enviarRevisionConCorreoNoVerificado() {
        when(anfitrion.isCorreoVerificado()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConCuentaRestringida() {
        when(anfitrion.isRestringido()).thenReturn(true);

        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConCuentaInactiva() {
        when(anfitrion.getEstado()).thenReturn(EstadoUsuario.INACTIVA);

        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConCincoIntentosFallidos() {
        when(anfitrion.getIntentosFallidos()).thenReturn(5);

        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConPublicacionInexistente() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verifyNoInteractions(alojamientoRepository);
    }

    @Test
    void enviarRevisionSinAlojamientoAsociado() {
        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionDePublicacionDeOtroAnfitrion() {
        prepararDatosValidos();

        when(alojamiento.getIdAnfitrion()).thenReturn(UUID.randomUUID());

        assertThrows(AccessDeniedException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionDePublicacionBloqueada() {
        prepararDatosValidos();

        when(publicacion.getEstado()).thenReturn("BLOQUEADA");

        assertThrows(IllegalStateException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionDePublicacionQueNoEsBorrador() {
        prepararDatosValidos();

        when(publicacion.getEstado()).thenReturn("ACTIVA");

        assertThrows(IllegalStateException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionConTituloVacio() {
        prepararDatosValidos();

        when(publicacion.getTitulo()).thenReturn(" ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        assertTrue(exception.getMessage().contains("título"));
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionSinPoliticaActiva() {
        prepararDatosValidos();

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        assertTrue(exception.getMessage().contains("política de cancelación"));
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionSinServicios() {
        prepararDatosValidos();

        when(publicacionServicioRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        assertTrue(exception.getMessage().contains("al menos un servicio"));
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionSinImagenes() {
        prepararDatosValidos();

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        assertTrue(exception.getMessage().contains("al menos una imagen"));
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionConRutaDeImagenInvalida() {
        prepararDatosValidos();

        ImagenPublicacion imagen = mock(ImagenPublicacion.class);
        when(imagen.getRutaImagen()).thenReturn(" ");

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of(imagen));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(anfitrion, idPublicacion));

        assertTrue(exception.getMessage().contains("rutas de imágenes válidas"));
        verify(publicacionRepository, never()).save(any());
    }
}
