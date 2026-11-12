
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import co.edu.unbosque.marketplace.alojamiento.contracts.ActualizarAlojamientoParaPublicacion;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionCommand;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.request.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.storage.AlmacenamientoImagenService;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionEdicionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private PublicacionServicioRepository publicacionServicioRepository;

    @Mock
    private ImagenPublicacionRepository imagenRepository;

    @Mock
    private AlojamientoPublicacionQuery alojamientoQuery;

    @Mock
    private AlojamientoPublicacionCommand alojamientoCommand;

    @Mock
    private UsuarioAdministracionQuery usuarioAdministracionQuery;

    @Mock
    private UsuarioPublicacionQuery usuarioPublicacionQuery;

    @Mock
    private ServicioPublicacionQuery servicioPublicacionQuery;

    @Mock
    private PoliticaCancelacionQuery politicaCancelacionQuery;

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
                publicacionServicioRepository,
                imagenRepository,
                alojamientoQuery,
                alojamientoCommand,
                usuarioAdministracionQuery,
                usuarioPublicacionQuery,
                servicioPublicacionQuery,
                politicaCancelacionQuery,
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

        when(solicitud.getUbicacion()).thenReturn("Calle 10");
        when(solicitud.getCiudad()).thenReturn("Bogotá");
        when(solicitud.getTipoAlojamiento()).thenReturn("Apartamento");
        when(solicitud.getTitulo()).thenReturn("Apartamento actualizado");
        when(solicitud.getDescripcion()).thenReturn("Nueva descripción");
        when(solicitud.getReglas()).thenReturn("No fumar");
        when(solicitud.getModoAceptacion()).thenReturn("AUTOMATICA");

        when(solicitud.getImagenes()).thenReturn(new ArrayList<>());

        return solicitud;
    }

    private Publicacion crearPublicacion() {

        Publicacion publicacion = mock(Publicacion.class);

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getTitulo()).thenReturn("Apartamento actualizado");

        return publicacion;
    }

    private AlojamientoPublicacionInfo crearAlojamiento() {

        AlojamientoPublicacionInfo alojamiento =
                mock(AlojamientoPublicacionInfo.class);

        when(alojamiento.idAlojamiento()).thenReturn(idAlojamiento);
        when(alojamiento.idAnfitrion()).thenReturn(idAnfitrion);

        return alojamiento;
    }

    private UsuarioPublicacionInfo crearAnfitrion() {

        UsuarioPublicacionInfo anfitrion =
                mock(UsuarioPublicacionInfo.class);

        when(anfitrion.esAnfitrion()).thenReturn(true);

        return anfitrion;
    }

    private void configurarPublicacionYPermisos(
            Publicacion publicacion,
            AlojamientoPublicacionInfo alojamiento) {

        UsuarioPublicacionInfo anfitrion = crearAnfitrion();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento))
                .thenReturn(Optional.of(alojamiento));

        when(usuarioAdministracionQuery.esAdministrador(idAnfitrion))
                .thenReturn(false);

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));
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

        EditarPublicacionRequest solicitud = crearSolicitudValida();

        when(solicitud.getCapacidad()).thenReturn(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarPublicacion(
                        idAnfitrion, idPublicacion, solicitud)
        );

        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void actualizarPublicacionInexistente() {

        EditarPublicacionRequest solicitud = crearSolicitudValida();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.empty());

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> service.actualizarPublicacion(
                        idAnfitrion, idPublicacion, solicitud)
        );

        verify(publicacionRepository).findById(idPublicacion);
        verifyNoInteractions(alojamientoQuery);
    }

    @Test
    void actualizarPublicacionSinAlojamientoAsociado() {

        EditarPublicacionRequest solicitud = crearSolicitudValida();

        Publicacion publicacion = crearPublicacion();

        when(publicacionRepository.findById(idPublicacion))
                .thenReturn(Optional.of(publicacion));

        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> service.actualizarPublicacion(
                        idAnfitrion, idPublicacion, solicitud)
        );

        verify(alojamientoQuery)
                .consultarDatosParaPublicacion(idAlojamiento);

        verifyNoInteractions(politicaCancelacionQuery);
    }

    @Test
    void actualizarPublicacionSinPermisos() {

        EditarPublicacionRequest solicitud = crearSolicitudValida();

        Publicacion publicacion = crearPublicacion();

        AlojamientoPublicacionInfo alojamiento = crearAlojamiento();

        configurarPublicacionYPermisos(publicacion, alojamiento);

        UsuarioPublicacionInfo usuario =
                mock(UsuarioPublicacionInfo.class);

        when(usuario.esAnfitrion()).thenReturn(false);

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                AccessDeniedException.class,
                () -> service.actualizarPublicacion(
                        idAnfitrion, idPublicacion, solicitud)
        );

        verifyNoInteractions(politicaCancelacionQuery);
    }

    @Test
    void actualizarPublicacionConPoliticaInexistente() {

        EditarPublicacionRequest solicitud = crearSolicitudValida();

        Publicacion publicacion = crearPublicacion();

        AlojamientoPublicacionInfo alojamiento = crearAlojamiento();

        configurarPublicacionYPermisos(publicacion, alojamiento);

        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarPublicacion(
                        idAnfitrion, idPublicacion, solicitud)
        );

        verify(politicaCancelacionQuery)
                .existePoliticaActiva(idPolitica);

        verifyNoInteractions(servicioPublicacionQuery);
    }

    @Test
    void actualizarPublicacionConServiciosDuplicados() {

        EditarPublicacionRequest solicitud = crearSolicitudValida();

        List<UUID> idsDuplicados = new ArrayList<>();
        idsDuplicados.add(idServicio);
        idsDuplicados.add(idServicio);

        when(solicitud.getIdsServicios()).thenReturn(idsDuplicados);

        Publicacion publicacion = crearPublicacion();

        AlojamientoPublicacionInfo alojamiento = crearAlojamiento();

        configurarPublicacionYPermisos(publicacion, alojamiento);

        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarPublicacion(
                        idAnfitrion, idPublicacion, solicitud)
        );

        verifyNoInteractions(servicioPublicacionQuery);
    }

    @Test
    void actualizarPublicacionCorrectamente() {

        EditarPublicacionRequest solicitud = crearSolicitudValida();

        Publicacion publicacion = crearPublicacion();

        AlojamientoPublicacionInfo alojamiento = crearAlojamiento();

        configurarPublicacionYPermisos(publicacion, alojamiento);

        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica))
                .thenReturn(true);

        ServicioInfo servicio = mock(ServicioInfo.class);

        when(servicioPublicacionQuery.consultarPorIds(anySet()))
                .thenReturn(List.of(servicio));

        PublicacionServicio relacionAnterior =
                mock(PublicacionServicio.class);

        UUID idServicioAnterior = UUID.randomUUID();

        when(relacionAnterior.getIdServicio())
                .thenReturn(idServicioAnterior);

        when(publicacionServicioRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(relacionAnterior));

        ImagenPublicacion imagenAnterior =
                mock(ImagenPublicacion.class);

        when(imagenAnterior.getRutaImagen())
                .thenReturn("imagenes/anterior.jpg");

        when(imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion))
                .thenReturn(List.of(imagenAnterior));

        PublicacionAlojamientoResponse respuesta =
                service.actualizarPublicacion(
                        idAnfitrion, idPublicacion, solicitud);

        assertNotNull(respuesta);

        verify(alojamientoCommand).actualizarAlojamiento(
                eq(idAlojamiento),
                any(ActualizarAlojamientoParaPublicacion.class)
        );

        verify(publicacionRepository).save(publicacion);

        verify(publicacionServicioRepository).deleteAll(anyList());
        verify(publicacionServicioRepository).saveAll(anyList());

        verify(imagenRepository)
                .findByIdPublicacionOrderByOrdenAsc(idPublicacion);

        verifyNoInteractions(almacenamientoImagenService);
    }
}
