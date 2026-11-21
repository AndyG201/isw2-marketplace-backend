package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionAdministracionResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionPublicaResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.ServicioAdicionalPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.SolicitudCambioResponse;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@ExtendWith(MockitoExtension.class)
class PublicacionDetallePublicoServiceTest {

    private static final UUID PUBLICACION = UUID.randomUUID();
    private static final UUID ALOJAMIENTO = UUID.randomUUID();
    private static final UUID POLITICA = UUID.randomUUID();

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoPublicacionQuery alojamientoQuery;

    @Mock
    private PublicacionConsultaService consultaService;

    @Mock
    private PublicacionServiciosAdicionalesSeleccionService adicionalesService;

    @Mock
    private PoliticaCancelacionQuery politicaQuery;

    @Mock
    private CambiosPublicacionService cambios;

    private PublicacionDetallePublicoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PublicacionDetallePublicoService(
                publicacionRepository, alojamientoQuery, consultaService, adicionalesService, politicaQuery, cambios);
    }

    private Publicacion publicacion(String estado) {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getEstado()).thenReturn(estado);
        return publicacion;
    }

    @Test
    void devuelveElDetalleConServiciosAdicionalesYPolitica() {
        Publicacion publicacion = publicacion("ACTIVA");
        when(publicacion.getIdAlojamiento()).thenReturn(ALOJAMIENTO);
        when(publicacion.getIdPolitica()).thenReturn(POLITICA);
        AlojamientoPublicacionInfo alojamiento = new AlojamientoPublicacionInfo(
                ALOJAMIENTO, UUID.randomUUID(), "Calle 1", "Bogotá", "Casa", 4);
        PublicacionDetalleResponse detalle = mock(PublicacionDetalleResponse.class);
        List<ServicioAdicionalPublicacionResponse> adicionales = List.of(
                new ServicioAdicionalPublicacionResponse(
                        UUID.randomUUID(), "Desayuno", null, new BigDecimal("20000"), true));

        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(publicacion));
        when(alojamientoQuery.consultarDatosParaPublicacion(ALOJAMIENTO)).thenReturn(Optional.of(alojamiento));
        when(consultaService.armarDetalle(publicacion, alojamiento)).thenReturn(detalle);
        when(adicionalesService.consultarDisponibles(PUBLICACION)).thenReturn(adicionales);
        when(politicaQuery.consultarPorId(POLITICA)).thenReturn(Optional.of(new PoliticaCancelacionInfo(
                POLITICA, "Flexible", 1, "Sin costo", 24, new BigDecimal("100"), true)));

        PublicacionPublicaResponse respuesta = servicio.consultar(PUBLICACION);

        assertSame(detalle, respuesta.publicacion());
        verify(detalle, never()).conPropietario(any());
        assertEquals(adicionales, respuesta.serviciosAdicionales());
        assertEquals("Flexible", respuesta.politica().getNombre());
        assertEquals(PUBLICACION, respuesta.politica().getIdPublicacion());
    }

    @Test
    void sinPoliticaLaDevuelveVacia() {
        Publicacion publicacion = publicacion("ACTIVA");
        when(publicacion.getIdAlojamiento()).thenReturn(ALOJAMIENTO);
        when(publicacion.getIdPolitica()).thenReturn(POLITICA);
        AlojamientoPublicacionInfo alojamiento = new AlojamientoPublicacionInfo(
                ALOJAMIENTO, UUID.randomUUID(), "Calle 1", "Bogotá", "Casa", 4);

        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(publicacion));
        when(alojamientoQuery.consultarDatosParaPublicacion(ALOJAMIENTO)).thenReturn(Optional.of(alojamiento));
        when(adicionalesService.consultarDisponibles(PUBLICACION)).thenReturn(List.of());
        when(politicaQuery.consultarPorId(POLITICA)).thenReturn(Optional.empty());

        assertNull(servicio.consultar(PUBLICACION).politica());
    }

    @Test
    void noMuestraPublicacionesQueNoEstanActivas() {
        Publicacion publicacion = publicacion("BORRADOR");
        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(publicacion));

        NoSuchElementException error =
                assertThrows(NoSuchElementException.class, () -> servicio.consultar(PUBLICACION));

        assertEquals("La publicación no existe o no está disponible.", error.getMessage());
        verifyNoInteractions(alojamientoQuery, consultaService, adicionalesService, politicaQuery);
    }

    @Test
    void informaSiLaPublicacionNoExisteOElIdentificadorFalta() {
        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> servicio.consultar(PUBLICACION));
        assertThrows(NoSuchElementException.class, () -> servicio.consultar(null));
    }

    @Test
    void laAdministracionVeLaPublicacionEnCualquierEstado() {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getIdAlojamiento()).thenReturn(ALOJAMIENTO);
        when(publicacion.getIdPolitica()).thenReturn(POLITICA);
        AlojamientoPublicacionInfo alojamiento = new AlojamientoPublicacionInfo(
                ALOJAMIENTO, UUID.randomUUID(), "Calle 1", "Bogotá", "Casa", 4, 1, 1, 1, "Ana Gómez");
        PublicacionDetalleResponse detalle = mock(PublicacionDetalleResponse.class);
        List<ServicioAdicionalPublicacionResponse> adicionales = List.of(
                new ServicioAdicionalPublicacionResponse(
                        UUID.randomUUID(), "Desayuno", null, new BigDecimal("20000"), true));

        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(publicacion));
        when(alojamientoQuery.consultarDatosParaPublicacion(ALOJAMIENTO)).thenReturn(Optional.of(alojamiento));
        when(consultaService.armarDetalle(publicacion, alojamiento)).thenReturn(detalle);
        when(adicionalesService.consultarRegistrados(PUBLICACION)).thenReturn(adicionales);
        when(politicaQuery.consultarPorId(POLITICA)).thenReturn(Optional.empty());

        SolicitudCambioResponse solicitud = new SolicitudCambioResponse(
                "Cambié las fotos", List.of("Fotos"), "ACTIVA", OffsetDateTime.now());
        when(cambios.pendiente(PUBLICACION)).thenReturn(Optional.of(solicitud));

        PublicacionAdministracionResponse respuesta = servicio.consultarParaAdministracion(PUBLICACION);

        assertSame(detalle, respuesta.publicacion());
        verify(detalle).conPropietario("Ana Gómez");
        assertEquals(adicionales, respuesta.serviciosAdicionales());
        assertNull(respuesta.politica());
        assertSame(solicitud, respuesta.solicitudCambios());
    }

    @Test
    void laAdministracionRecibeNoEncontradaSiLaPublicacionNoExiste() {
        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.empty());

        NoSuchElementException error = assertThrows(NoSuchElementException.class,
                () -> servicio.consultarParaAdministracion(PUBLICACION));

        assertEquals("La publicación no existe.", error.getMessage());
        assertThrows(NoSuchElementException.class, () -> servicio.consultarParaAdministracion(null));
        verifyNoInteractions(alojamientoQuery, consultaService, adicionalesService, politicaQuery);
    }

    @Test
    void fallaSiNoEncuentraElAlojamiento() {
        Publicacion publicacion = publicacion("ACTIVA");
        when(publicacion.getIdAlojamiento()).thenReturn(ALOJAMIENTO);
        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(publicacion));
        when(alojamientoQuery.consultarDatosParaPublicacion(ALOJAMIENTO)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> servicio.consultar(PUBLICACION));
    }
}
