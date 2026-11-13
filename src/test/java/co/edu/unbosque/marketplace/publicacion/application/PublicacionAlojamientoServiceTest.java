
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionCommand;
import co.edu.unbosque.marketplace.alojamiento.contracts.CrearAlojamientoParaPublicacion;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.request.CrearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.storage.AlmacenamientoImagenService;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionAlojamientoServiceTest {

    @Mock
    private AlojamientoPublicacionCommand alojamientoCommand;

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private PublicacionServicioRepository publicacionServicioRepository;

    @Mock
    private ImagenPublicacionRepository imagenPublicacionRepository;

    @Mock
    private ServicioPublicacionQuery servicioPublicacionQuery;

    @Mock
    private PoliticaCancelacionQuery politicaCancelacionQuery;

    @Mock
    private UsuarioPublicacionQuery usuarioPublicacionQuery;

    @Mock
    private AlmacenamientoImagenService almacenamientoImagenService;

    private PublicacionAlojamientoService service;

    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();
    private final UUID idServicio = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        service = new PublicacionAlojamientoService(
                alojamientoCommand,
                publicacionRepository,
                publicacionServicioRepository,
                imagenPublicacionRepository,
                servicioPublicacionQuery,
                politicaCancelacionQuery,
                usuarioPublicacionQuery,
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
        when(solicitud.getModoAceptacion()).thenReturn(" INMEDIATA ");

        when(imagen.isEmpty()).thenReturn(false);

        return solicitud;
    }

    private void configurarAnfitrionValido() {

        UsuarioPublicacionInfo anfitrion =
                mock(UsuarioPublicacionInfo.class);

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(anfitrion));

        when(anfitrion.esAnfitrion()).thenReturn(true);
        when(anfitrion.cuentaHabilitadaParaPublicar()).thenReturn(true);

        when(alojamientoCommand.crearAlojamiento(
                any(CrearAlojamientoParaPublicacion.class)))
                .thenReturn(idAlojamiento);
    }

    @Test
    void crearPublicacionCorrectamente() {

        CrearPublicacionRequest solicitud = crearSolicitudValida();

        configurarAnfitrionValido();

        ServicioInfo servicio = mock(ServicioInfo.class);

        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica))
                .thenReturn(true);

        when(servicioPublicacionQuery.consultarPorIds(anySet()))
                .thenReturn(List.of(servicio));

        when(almacenamientoImagenService.guardar(
                solicitud.getImagenes().get(0)))
                .thenReturn("imagenes/foto.jpg");

        PublicacionAlojamientoResponse respuesta =
                service.crearPublicacion(idAnfitrion, solicitud);

        assertNotNull(respuesta);

        verify(alojamientoCommand).crearAlojamiento(
                any(CrearAlojamientoParaPublicacion.class));

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

        verifyNoInteractions(usuarioPublicacionQuery);
    }

    @Test
    void crearPublicacionConAnfitrionInexistente() {

        CrearPublicacionRequest solicitud = crearSolicitudValida();

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPublicacion(idAnfitrion, solicitud)
        );

        verify(usuarioPublicacionQuery)
                .consultarParaPublicacion(idAnfitrion);

        verifyNoInteractions(politicaCancelacionQuery);
    }

    @Test
    void crearPublicacionConRolIncorrecto() {

        CrearPublicacionRequest solicitud = crearSolicitudValida();

        UsuarioPublicacionInfo usuario =
                mock(UsuarioPublicacionInfo.class);

        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(usuario));

        when(usuario.esAnfitrion()).thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> service.crearPublicacion(idAnfitrion, solicitud)
        );

        verifyNoInteractions(politicaCancelacionQuery);
    }

    @Test
    void crearPublicacionConPoliticaInexistente() {

        CrearPublicacionRequest solicitud = crearSolicitudValida();

        configurarAnfitrionValido();

        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPublicacion(idAnfitrion, solicitud)
        );

        verify(politicaCancelacionQuery)
                .existePoliticaActiva(idPolitica);

        verifyNoInteractions(servicioPublicacionQuery);
    }
}
