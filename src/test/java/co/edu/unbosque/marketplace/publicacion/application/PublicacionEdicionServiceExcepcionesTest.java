package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionCommand;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.publicacion.application.dto.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.port.AlmacenamientoImagenes;
import co.edu.unbosque.marketplace.publicacion.application.port.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionEdicionServiceExcepcionesTest {

    @Mock private PublicacionRepository publicacionRepository;
    @Mock private PublicacionServicioRepository publicacionServicioRepository;
    @Mock private ImagenPublicacionRepository imagenRepository;
    @Mock private AlojamientoPublicacionQuery alojamientoQuery;
    @Mock private AlojamientoPublicacionCommand alojamientoCommand;
    @Mock private UsuarioAdministracionQuery usuarioAdministracionQuery;
    @Mock private UsuarioPublicacionQuery usuarioPublicacionQuery;
    @Mock private ServicioPublicacionQuery servicioPublicacionQuery;
    @Mock private PoliticaCancelacionQuery politicaCancelacionQuery;
    @Mock private AlmacenamientoImagenes almacenamientoImagenes;
    @Mock private CambiosPublicacionService cambios;
    @InjectMocks private PublicacionEdicionService service;

    private final UUID idActor = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();

    private EditarPublicacionRequest solicitudValida() {
        EditarPublicacionRequest solicitud = mock(EditarPublicacionRequest.class);
        lenient().when(solicitud.getCapacidad()).thenReturn(2);
        lenient().when(solicitud.getPrecioPorNoche()).thenReturn(new BigDecimal("100000"));
        lenient().when(solicitud.getIdsServicios()).thenReturn(new ArrayList<>(List.of(UUID.randomUUID())));
        lenient().when(solicitud.getIdPolitica()).thenReturn(idPolitica);
        return solicitud;
    }

    private void solicitudInvalida(Consumer<EditarPublicacionRequest> ajuste) {
        EditarPublicacionRequest solicitud = solicitudValida();
        ajuste.accept(solicitud);
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarPublicacion(idActor, idPublicacion, solicitud));
    }

    private void publicacionYAlojamiento() {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        lenient().when(publicacion.getEstado()).thenReturn("BORRADOR");
        when(publicacionRepository.findById(idPublicacion)).thenReturn(Optional.of(publicacion));
        AlojamientoPublicacionInfo alojamiento = mock(AlojamientoPublicacionInfo.class);
        lenient().when(alojamiento.capacidad()).thenReturn(2);
        when(alojamientoQuery.consultarDatosParaPublicacion(idAlojamiento)).thenReturn(Optional.of(alojamiento));
    }

    @Test
    void rechazaActorOIdentificadoresNulos() {
        assertThrows(AccesoNoPermitidoException.class,
                () -> service.actualizarPublicacion(null, idPublicacion, solicitudValida()));
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarPublicacion(idActor, null, solicitudValida()));
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarPublicacion(idActor, idPublicacion, null));
    }

    @Test
    void rechazaDatosInvalidosDeLaSolicitud() {
        solicitudInvalida(s -> when(s.getCapacidad()).thenReturn(0));
        solicitudInvalida(s -> when(s.getPrecioPorNoche()).thenReturn(BigDecimal.ZERO));
        solicitudInvalida(s -> when(s.getTarifaLimpieza()).thenReturn(new BigDecimal("-1")));
        solicitudInvalida(s -> when(s.getIdsServicios()).thenReturn(new ArrayList<>()));
        solicitudInvalida(s -> when(s.getIdsServicios()).thenReturn(Arrays.asList(UUID.randomUUID(), null)));
        solicitudInvalida(s -> when(s.getIdPolitica()).thenReturn(null));
        solicitudInvalida(s -> when(s.getImagenes()).thenReturn(
                new ArrayList<MultipartFile>(Arrays.asList(new MultipartFile[11]))));
    }

    @Test
    void rechazaAlUsuarioSinPermisosSobreLaPublicacion() {
        publicacionYAlojamiento();
        when(usuarioAdministracionQuery.esAdministrador(idActor)).thenReturn(false);
        when(usuarioPublicacionQuery.consultarParaPublicacion(idActor)).thenReturn(Optional.empty());

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.actualizarPublicacion(idActor, idPublicacion, solicitudValida()));
    }

    @Test
    void rechazaAUnAnfitrionQueNoEsElPropietario() {
        publicacionYAlojamiento();
        when(usuarioAdministracionQuery.esAdministrador(idActor)).thenReturn(false);
        when(usuarioPublicacionQuery.consultarParaPublicacion(idActor))
                .thenReturn(Optional.of(new UsuarioPublicacionInfo(true, true, true, false, true, 0)));

        assertThrows(AccesoNoPermitidoException.class,
                () -> service.actualizarPublicacion(idActor, idPublicacion, solicitudValida()));
    }

    @Test
    void rechazaPoliticaInactivaYServiciosRepetidosOInexistentes() {
        publicacionYAlojamiento();
        when(usuarioAdministracionQuery.esAdministrador(idActor)).thenReturn(true);
        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica)).thenReturn(false, true, true);
        UUID repetido = UUID.randomUUID();
        EditarPublicacionRequest repetidos = solicitudValida();
        when(repetidos.getIdsServicios()).thenReturn(new ArrayList<>(List.of(repetido, repetido)));
        when(servicioPublicacionQuery.consultarPorIds(anySet())).thenReturn(List.<ServicioInfo>of());

        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarPublicacion(idActor, idPublicacion, solicitudValida()));
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarPublicacion(idActor, idPublicacion, repetidos));
        assertThrows(DatosInvalidosException.class,
                () -> service.actualizarPublicacion(idActor, idPublicacion, solicitudValida()));
    }
}
