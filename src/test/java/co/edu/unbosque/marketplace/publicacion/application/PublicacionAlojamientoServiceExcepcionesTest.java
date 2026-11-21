package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionCommand;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.publicacion.application.dto.CrearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.port.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.application.port.AlmacenamientoImagenes;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionAlojamientoServiceExcepcionesTest {

    @Mock private AlojamientoPublicacionCommand alojamientoCommand;
    @Mock private PublicacionRepository publicacionRepository;
    @Mock private PublicacionServicioRepository publicacionServicioRepository;
    @Mock private ImagenPublicacionRepository imagenPublicacionRepository;
    @Mock private ServicioPublicacionQuery servicioPublicacionQuery;
    @Mock private PoliticaCancelacionQuery politicaCancelacionQuery;
    @Mock private UsuarioPublicacionQuery usuarioPublicacionQuery;
    @Mock private AlmacenamientoImagenes almacenamientoImagenes;
    @InjectMocks private PublicacionAlojamientoService service;

    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();

    private CrearPublicacionRequest solicitudValida() {
        CrearPublicacionRequest solicitud = mock(CrearPublicacionRequest.class);
        lenient().when(solicitud.getCapacidad()).thenReturn(2);
        lenient().when(solicitud.getPrecioPorNoche()).thenReturn(new BigDecimal("100000"));
        lenient().when(solicitud.getTarifaLimpieza()).thenReturn(null);
        lenient().when(solicitud.getIdsServicios()).thenReturn(new java.util.ArrayList<>(List.of(UUID.randomUUID())));
        lenient().when(solicitud.getImagenes()).thenReturn(List.<MultipartFile>of(
                new MockMultipartFile("i", "a.jpg", "image/jpeg", new byte[] {1})));
        lenient().when(solicitud.getIdPolitica()).thenReturn(idPolitica);
        return solicitud;
    }

    private void solicitudInvalida(Consumer<CrearPublicacionRequest> ajuste) {
        CrearPublicacionRequest solicitud = solicitudValida();
        ajuste.accept(solicitud);
        assertThrows(DatosInvalidosException.class, () -> service.crearPublicacion(idAnfitrion, solicitud));
    }

    @Test
    void rechazaSolicitudNulaOSinAnfitrion() {
        assertThrows(DatosInvalidosException.class, () -> service.crearPublicacion(null, solicitudValida()));
        assertThrows(DatosInvalidosException.class, () -> service.crearPublicacion(idAnfitrion, null));
    }

    @Test
    void rechazaDatosNumericosInvalidos() {
        solicitudInvalida(s -> when(s.getCapacidad()).thenReturn(0));
        solicitudInvalida(s -> when(s.getPrecioPorNoche()).thenReturn(BigDecimal.ZERO));
        solicitudInvalida(s -> when(s.getTarifaLimpieza()).thenReturn(new BigDecimal("-1")));
    }

    @Test
    void rechazaServiciosEImagenesInvalidos() {
        solicitudInvalida(s -> when(s.getIdsServicios()).thenReturn(new java.util.ArrayList<>()));
        solicitudInvalida(s -> when(s.getIdsServicios()).thenReturn(Arrays.asList(UUID.randomUUID(), null)));
        solicitudInvalida(s -> when(s.getImagenes()).thenReturn(new java.util.ArrayList<>()));
        solicitudInvalida(s -> when(s.getImagenes()).thenReturn(List.<MultipartFile>of(
                new MockMultipartFile("i", "a.jpg", "image/jpeg", new byte[0]))));
        solicitudInvalida(s -> when(s.getIdPolitica()).thenReturn(null));
    }

    @Test
    void rechazaAnfitrionInexistente() {
        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion)).thenReturn(Optional.empty());

        assertThrows(DatosInvalidosException.class, () -> service.crearPublicacion(idAnfitrion, solicitudValida()));
    }

    @Test
    void rechazaUsuarioQueNoEsAnfitrionOSinCuentaHabilitada() {
        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion)).thenReturn(
                Optional.of(new UsuarioPublicacionInfo(false, true, true, false, true, 0)),
                Optional.of(new UsuarioPublicacionInfo(true, false, true, false, true, 0)));

        assertThrows(ConflictoNegocioException.class, () -> service.crearPublicacion(idAnfitrion, solicitudValida()));
        assertThrows(ConflictoNegocioException.class, () -> service.crearPublicacion(idAnfitrion, solicitudValida()));
    }

    @Test
    void rechazaPoliticaInactiva() {
        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(new UsuarioPublicacionInfo(true, true, true, false, true, 0)));
        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica)).thenReturn(false);

        assertThrows(DatosInvalidosException.class, () -> service.crearPublicacion(idAnfitrion, solicitudValida()));
    }

    @Test
    void rechazaServiciosRepetidosOInexistentes() {
        when(usuarioPublicacionQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(new UsuarioPublicacionInfo(true, true, true, false, true, 0)));
        when(politicaCancelacionQuery.existePoliticaActiva(idPolitica)).thenReturn(true);
        UUID repetido = UUID.randomUUID();
        CrearPublicacionRequest conRepetidos = solicitudValida();
        when(conRepetidos.getIdsServicios()).thenReturn(new java.util.ArrayList<>(List.of(repetido, repetido)));
        CrearPublicacionRequest inexistentes = solicitudValida();
        when(servicioPublicacionQuery.consultarPorIds(org.mockito.ArgumentMatchers.anySet()))
                .thenReturn(List.<ServicioInfo>of());

        assertThrows(DatosInvalidosException.class, () -> service.crearPublicacion(idAnfitrion, conRepetidos));
        assertThrows(DatosInvalidosException.class, () -> service.crearPublicacion(idAnfitrion, inexistentes));
    }
}
