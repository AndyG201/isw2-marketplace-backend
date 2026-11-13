package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaPublicacionCommand;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.EnviarRevisionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionRevisionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private ImagenPublicacionRepository imagenRepository;

    @Mock
    private PublicacionServicioRepository publicacionServicioRepository;

    @Mock
    private UsuarioPublicacionQuery usuarioPublicacionQuery;

    @Mock
    private AlojamientoPublicacionQuery alojamientoPublicacionQuery;

    @Mock
    private ServicioPublicacionQuery servicioPublicacionQuery;

    @Mock
    private PoliticaCancelacionQuery politicaCancelacionQuery;

    @Mock
    private AuditoriaPublicacionCommand auditoriaPublicacionCommand;

    @Mock
    private UsuarioPublicacionInfo anfitrion;

    @Mock
    private AlojamientoPublicacionInfo alojamiento;

    @Mock
    private Publicacion publicacion;

    @InjectMocks
    private PublicacionRevisionService service;

    private UUID idAnfitrion;
    private UUID idPublicacion;
    private UUID idAlojamiento;
    private UUID idPolitica;
    private UUID idServicio;

    @BeforeEach
    void setUp() {

        idAnfitrion = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        idPolitica = UUID.randomUUID();
        idServicio = UUID.randomUUID();

        when(anfitrion.esAnfitrion()).thenReturn(true);
        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(true);

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getEstado()).thenReturn("BORRADOR");

        when(alojamiento.idAlojamiento()).thenReturn(idAlojamiento);
        when(alojamiento.idAnfitrion()).thenReturn(idAnfitrion);
        when(alojamiento.ubicacion()).thenReturn("Centro");
        when(alojamiento.ciudad()).thenReturn("Bogotá");
        when(alojamiento.tipoAlojamiento()).thenReturn("APARTAMENTO");
        when(alojamiento.capacidad()).thenReturn(4);
    }

    private void configurarAnfitrionValido() {

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));
    }

    private void prepararDatosValidos() {

        configurarAnfitrionValido();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoPublicacionQuery
                .consultarDatosParaPublicacion(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(publicacion.getTitulo()).thenReturn("Apartamento");
        when(publicacion.getDescripcion()).thenReturn("Apartamento amoblado");
        when(publicacion.getReglas()).thenReturn("No fumar");

        when(publicacion.getPrecioPorNoche())
                .thenReturn(new BigDecimal("150000"));

        when(publicacion.getTarifaLimpieza())
                .thenReturn(new BigDecimal("20000"));

        when(publicacion.getModoAceptacion()).thenReturn("INMEDIATA");
        when(publicacion.getIdPolitica()).thenReturn(idPolitica);

        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica))
                .thenReturn(true);

        PublicacionServicio relacion = mock(PublicacionServicio.class);
        when(relacion.getIdServicio()).thenReturn(idServicio);

        when(publicacionServicioRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(relacion));

        ServicioInfo servicio = mock(ServicioInfo.class);

        when(servicioPublicacionQuery.consultarPorIds(List.of(idServicio)))
                .thenReturn(List.of(servicio));

        ImagenPublicacion imagen = mock(ImagenPublicacion.class);
        when(imagen.getRutaImagen())
                .thenReturn("imagenes/apartamento.jpg");

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of(imagen));
    }

    @Test
    void enviarRevisionCorrectamente() {

        prepararDatosValidos();

        EnviarRevisionResponse respuesta =
                service.enviarRevision(idAnfitrion, idPublicacion);

        assertNotNull(respuesta);

        verify(publicacion).setEstado("PENDIENTE_REVISION");
        verify(publicacion).setFechaActualizacion(any());

        verify(publicacionRepository).save(publicacion);

        verify(auditoriaPublicacionCommand)
                .registrarEvento(any());
    }

    @Test
    void enviarRevisionConAnfitrionNulo() {

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(null, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void enviarRevisionConIdentificadorDeAnfitrionNulo() {

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(null, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConAnfitrionInexistente() {

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(
                publicacionRepository,
                alojamientoPublicacionQuery
        );
    }

    @Test
    void enviarRevisionConRolIncorrecto() {

        configurarAnfitrionValido();

        when(anfitrion.esAnfitrion()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConCorreoNoVerificado() {

        configurarAnfitrionValido();

        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConCuentaRestringida() {

        configurarAnfitrionValido();

        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConCuentaInactiva() {

        configurarAnfitrionValido();

        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConCincoIntentosFallidos() {

        configurarAnfitrionValido();

        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enviarRevisionConPublicacionInexistente() {

        configurarAnfitrionValido();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verifyNoInteractions(alojamientoPublicacionQuery);
    }

    @Test
    void enviarRevisionSinAlojamientoAsociado() {

        configurarAnfitrionValido();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoPublicacionQuery
                .consultarDatosParaPublicacion(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionDePublicacionDeOtroAnfitrion() {

        prepararDatosValidos();

        when(alojamiento.idAnfitrion())
                .thenReturn(UUID.randomUUID());

        assertThrows(
                AccessDeniedException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionDePublicacionBloqueada() {

        prepararDatosValidos();

        when(publicacion.getEstado()).thenReturn("BLOQUEADA");

        assertThrows(
                IllegalStateException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionDePublicacionQueNoEsBorrador() {

        prepararDatosValidos();

        when(publicacion.getEstado()).thenReturn("ACTIVA");

        assertThrows(
                IllegalStateException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionConTituloVacio() {

        prepararDatosValidos();

        when(publicacion.getTitulo()).thenReturn(" ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        assertTrue(exception.getMessage().contains("título"));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionSinPoliticaActiva() {

        prepararDatosValidos();

        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        assertTrue(exception.getMessage()
                .contains("política de cancelación"));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionSinServicios() {

        prepararDatosValidos();

        when(publicacionServicioRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        assertTrue(exception.getMessage()
                .contains("al menos un servicio"));

        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void enviarRevisionSinImagenes() {

        prepararDatosValidos();

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        assertTrue(exception.getMessage()
                .contains("al menos una imagen"));

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
                () -> service.enviarRevision(idAnfitrion, idPublicacion)
        );

        assertTrue(exception.getMessage()
                .contains("rutas de imágenes válidas"));

        verify(publicacionRepository, never()).save(any());
    }
}
