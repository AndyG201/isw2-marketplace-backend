package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.publicacion.api.request.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.storage.AlmacenamientoImagenService;
import co.edu.unbosque.marketplace.servicio.domain.Servicio;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionEdicionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoRepository alojamientoRepository;

    @Mock
    private PublicacionServicioRepository publicacionServicioRepository;

    @Mock
    private ImagenPublicacionRepository imagenRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private PoliticaCancelacionRepository politicaRepository;

    @Mock
    private AlmacenamientoImagenService almacenamientoImagenService;

    private PublicacionEdicionService service;

    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();
    private final UUID idServicio = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new PublicacionEdicionService(
                publicacionRepository,
                alojamientoRepository,
                publicacionServicioRepository,
                imagenRepository,
                servicioRepository,
                politicaRepository,
                almacenamientoImagenService
        );
    }

    private EditarPublicacionRequest crearSolicitudValida() {
        EditarPublicacionRequest solicitud =
                mock(EditarPublicacionRequest.class);

        List<UUID> idsServicios = new ArrayList<>();
        idsServicios.add(idServicio);

        when(solicitud.getCapacidad()).thenReturn(3);
        when(solicitud.getPrecioPorNoche())
                .thenReturn(new BigDecimal("150000"));
        when(solicitud.getTarifaLimpieza())
                .thenReturn(new BigDecimal("20000"));
        when(solicitud.getIdsServicios()).thenReturn(idsServicios);
        when(solicitud.getIdPolitica()).thenReturn(idPolitica);

        when(solicitud.getUbicacion()).thenReturn(" Calle 10 ");
        when(solicitud.getCiudad()).thenReturn(" Bogotá ");
        when(solicitud.getTipoAlojamiento()).thenReturn(" Apartamento ");
        when(solicitud.getTitulo()).thenReturn(" Apartamento actualizado ");
        when(solicitud.getDescripcion()).thenReturn(" Nueva descripción ");
        when(solicitud.getReglas()).thenReturn(" No fumar ");
        when(solicitud.getModoAceptacion()).thenReturn(" automatica ");

        when(solicitud.getImagenes()).thenReturn(new ArrayList<>());

        return solicitud;
    }

    private Usuario crearAnfitrion() {
        Usuario actor = mock(
                Usuario.class,
                Answers.RETURNS_DEEP_STUBS
        );

        when(actor.getIdUsuario()).thenReturn(idAnfitrion);
        when(actor.getRol().getNombre()).thenReturn("ANFITRION");

        return actor;
    }

    private Publicacion crearPublicacion() {
        Publicacion publicacion = mock(Publicacion.class);

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getTitulo()).thenReturn("Apartamento actualizado");

        return publicacion;
    }

    private Alojamiento crearAlojamiento() {
        Alojamiento alojamiento = mock(Alojamiento.class);

        when(alojamiento.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(alojamiento.getIdAnfitrion()).thenReturn(idAnfitrion);

        return alojamiento;
    }

    private void prepararPublicacionYPermisos(
            Usuario actor,
            Publicacion publicacion,
            Alojamiento alojamiento) {

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));
    }

    @Test
    void actualizarPublicacionConActorNulo() {
        EditarPublicacionRequest solicitud = crearSolicitudValida();

        assertThrows(
                AccessDeniedException.class,
                () -> service.actualizarPublicacion(
                        null, idPublicacion, solicitud)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void actualizarPublicacionConCapacidadInvalida() {
        Usuario actor = crearAnfitrion();
        EditarPublicacionRequest solicitud = crearSolicitudValida();

        when(solicitud.getCapacidad()).thenReturn(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarPublicacion(
                        actor, idPublicacion, solicitud)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void actualizarPublicacionInexistente() {
        Usuario actor = crearAnfitrion();
        EditarPublicacionRequest solicitud = crearSolicitudValida();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> service.actualizarPublicacion(
                        actor, idPublicacion, solicitud)
        );

        verify(publicacionRepository).findById(idPublicacion);
        verifyNoInteractions(alojamientoRepository);
    }

    @Test
    void actualizarPublicacionSinAlojamientoAsociado() {
        Usuario actor = crearAnfitrion();
        EditarPublicacionRequest solicitud = crearSolicitudValida();
        Publicacion publicacion = crearPublicacion();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoRepository.findById(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.actualizarPublicacion(
                        actor, idPublicacion, solicitud)
        );

        verify(alojamientoRepository).findById(idAlojamiento);
        verifyNoInteractions(politicaRepository);
    }

    @Test
    void actualizarPublicacionSinPermisos() {
        Usuario actor = crearAnfitrion();
        EditarPublicacionRequest solicitud = crearSolicitudValida();

        when(actor.getRol().getNombre()).thenReturn("CLIENTE");

        Publicacion publicacion = crearPublicacion();
        Alojamiento alojamiento = crearAlojamiento();

        prepararPublicacionYPermisos(actor, publicacion, alojamiento);

        assertThrows(
                AccessDeniedException.class,
                () -> service.actualizarPublicacion(
                        actor, idPublicacion, solicitud)
        );

        verifyNoInteractions(politicaRepository);
    }

    @Test
    void actualizarPublicacionConPoliticaInexistente() {
        Usuario actor = crearAnfitrion();
        EditarPublicacionRequest solicitud = crearSolicitudValida();

        Publicacion publicacion = crearPublicacion();
        Alojamiento alojamiento = crearAlojamiento();

        prepararPublicacionYPermisos(actor, publicacion, alojamiento);

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarPublicacion(
                        actor, idPublicacion, solicitud)
        );

        verify(politicaRepository)
                .findByIdPoliticaAndActivaTrue(idPolitica);

        verifyNoInteractions(servicioRepository);
    }

    @Test
    void actualizarPublicacionConServiciosDuplicados() {
        Usuario actor = crearAnfitrion();
        EditarPublicacionRequest solicitud = crearSolicitudValida();

        List<UUID> idsDuplicados = new ArrayList<>();
        idsDuplicados.add(idServicio);
        idsDuplicados.add(idServicio);

        when(solicitud.getIdsServicios()).thenReturn(idsDuplicados);

        Publicacion publicacion = crearPublicacion();
        Alojamiento alojamiento = crearAlojamiento();

        prepararPublicacionYPermisos(actor, publicacion, alojamiento);

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(mock(
                        co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion.class)));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarPublicacion(
                        actor, idPublicacion, solicitud)
        );

        verifyNoInteractions(servicioRepository);
    }

    @Test
    void actualizarPublicacionCorrectamente() {
        Usuario actor = crearAnfitrion();
        EditarPublicacionRequest solicitud = crearSolicitudValida();

        Publicacion publicacion = crearPublicacion();
        Alojamiento alojamiento = crearAlojamiento();

        prepararPublicacionYPermisos(actor, publicacion, alojamiento);

        var politica = mock(
                co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion.class);

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(politica));

        Servicio servicio = mock(Servicio.class);

        when(servicioRepository.findAllById(any()))
                .thenReturn(List.of(servicio));

        PublicacionServicio relacionAnterior =
                mock(PublicacionServicio.class);

        UUID idServicioAnterior = UUID.randomUUID();

        when(relacionAnterior.getIdServicio())
                .thenReturn(idServicioAnterior);

        when(publicacionServicioRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(relacionAnterior));

        ImagenPublicacion imagenAnterior = mock(ImagenPublicacion.class);

        when(imagenAnterior.getRutaImagen())
                .thenReturn("imagenes/anterior.jpg");

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of(imagenAnterior));

        PublicacionAlojamientoResponse respuesta =
                service.actualizarPublicacion(
                        actor, idPublicacion, solicitud);

        assertNotNull(respuesta);

        verify(alojamientoRepository).save(alojamiento);
        verify(publicacionRepository).save(publicacion);

        verify(publicacionServicioRepository).deleteAll(anyList());
        verify(publicacionServicioRepository).saveAll(anyList());

        verify(imagenRepository)
                .findByIdPublicacionOrderByOrdenAsc(idPublicacion);

        verifyNoInteractions(almacenamientoImagenService);
    }
}
