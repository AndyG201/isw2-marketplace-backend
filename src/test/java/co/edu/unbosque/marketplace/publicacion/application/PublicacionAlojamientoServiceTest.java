package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.publicacion.api.request.CrearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.storage.AlmacenamientoImagenService;
import co.edu.unbosque.marketplace.servicio.domain.Servicio;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionAlojamientoServiceTest {

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private PublicacionServicioRepository publicacionServicioRepository;

    @Mock
    private ImagenPublicacionRepository imagenPublicacionRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private PoliticaCancelacionRepository politicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlmacenamientoImagenService almacenamientoImagenService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Usuario anfitrion;

    private PublicacionAlojamientoService service;

    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();
    private final UUID idServicio = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PublicacionAlojamientoService(
                alojamientoRepository,
                publicacionRepository,
                publicacionServicioRepository,
                imagenPublicacionRepository,
                servicioRepository,
                politicaRepository,
                usuarioRepository,
                almacenamientoImagenService
        );

        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private CrearPublicacionRequest crearSolicitudValida() {
        CrearPublicacionRequest solicitud =
                mock(CrearPublicacionRequest.class);

        MultipartFile imagen = mock(MultipartFile.class);

        List<UUID> idsServicios = new ArrayList<>();
        idsServicios.add(idServicio);

        List<MultipartFile> imagenes = new ArrayList<>();
        imagenes.add(imagen);

        when(solicitud.getCapacidad()).thenReturn(2);
        when(solicitud.getPrecioPorNoche())
                .thenReturn(new BigDecimal("150000"));
        when(solicitud.getIdsServicios()).thenReturn(idsServicios);
        when(solicitud.getImagenes()).thenReturn(imagenes);
        when(solicitud.getIdPolitica()).thenReturn(idPolitica);

        when(solicitud.getUbicacion()).thenReturn(" Calle 10 ");
        when(solicitud.getCiudad()).thenReturn(" Bogotá ");
        when(solicitud.getTipoAlojamiento()).thenReturn(" Apartamento ");
        when(solicitud.getTitulo()).thenReturn(" Alojamiento de prueba ");
        when(solicitud.getDescripcion())
                .thenReturn(" Descripción de prueba ");
        when(solicitud.getReglas()).thenReturn(" No fumar ");
        when(solicitud.getModoAceptacion()).thenReturn(" AUTOMATICA ");

        when(imagen.isEmpty()).thenReturn(false);

        return solicitud;
    }

    private void configurarAnfitrionValido() {
        when(usuarioRepository.findById(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));

        when(anfitrion.getRol().getNombre())
                .thenReturn("ANFITRION");

        when(anfitrion.isCorreoVerificado()).thenReturn(true);
        when(anfitrion.getEstado()).thenReturn(EstadoUsuario.ACTIVA);
        when(anfitrion.isRestringido()).thenReturn(false);
        when(anfitrion.getIntentosFallidos()).thenReturn(0);
    }

    @Test
    void crearPublicacionCorrectamente() {
        CrearPublicacionRequest solicitud = crearSolicitudValida();

        configurarAnfitrionValido();

        PoliticaCancelacion politica = mock(PoliticaCancelacion.class);
        Servicio servicio = mock(Servicio.class);

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(politica));

        when(politica.getIdPolitica()).thenReturn(idPolitica);

        when(servicioRepository.findAllById(any()))
                .thenReturn(new ArrayList<>(List.of(servicio)));

        when(almacenamientoImagenService.guardar(
                solicitud.getImagenes().get(0)))
                .thenReturn("imagenes/foto.jpg");

        PublicacionAlojamientoResponse respuesta =
                service.crearPublicacion(idAnfitrion, solicitud);

        assertNotNull(respuesta);

        verify(alojamientoRepository).save(any());
        verify(publicacionRepository).save(any());
        verify(publicacionServicioRepository).save(any());
        verify(imagenPublicacionRepository).save(any());

        verify(almacenamientoImagenService)
                .guardar(solicitud.getImagenes().get(0));
    }

    @Test
    void crearPublicacionConCapacidadInvalida() {
        CrearPublicacionRequest solicitud = crearSolicitudValida();

        when(solicitud.getCapacidad()).thenReturn(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPublicacion(idAnfitrion, solicitud)
        );

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void crearPublicacionConAnfitrionInexistente() {
        CrearPublicacionRequest solicitud = crearSolicitudValida();

        when(usuarioRepository.findById(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPublicacion(idAnfitrion, solicitud)
        );

        verify(usuarioRepository).findById(idAnfitrion);
        verifyNoInteractions(politicaRepository);
    }

    @Test
    void crearPublicacionConRolIncorrecto() {
        CrearPublicacionRequest solicitud = crearSolicitudValida();

        when(usuarioRepository.findById(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));

        when(anfitrion.getRol().getNombre())
                .thenReturn("CLIENTE");

        assertThrows(
                IllegalStateException.class,
                () -> service.crearPublicacion(idAnfitrion, solicitud)
        );

        verifyNoInteractions(politicaRepository);
    }

    @Test
    void crearPublicacionConPoliticaInexistente() {
        CrearPublicacionRequest solicitud = crearSolicitudValida();

        configurarAnfitrionValido();

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPublicacion(idAnfitrion, solicitud)
        );

        verify(politicaRepository)
                .findByIdPoliticaAndActivaTrue(idPolitica);

        verifyNoInteractions(servicioRepository);
    }
}