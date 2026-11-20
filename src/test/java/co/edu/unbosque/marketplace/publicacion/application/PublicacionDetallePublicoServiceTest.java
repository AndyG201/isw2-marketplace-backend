package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionDetalleResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionPublicaResponse;
import co.edu.unbosque.marketplace.publicacion.api.response.ServicioAdicionalPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;

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

    private PublicacionDetallePublicoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PublicacionDetallePublicoService(
                publicacionRepository, alojamientoQuery, consultaService, adicionalesService, politicaQuery);
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
    void fallaSiNoEncuentraElAlojamiento() {
        Publicacion publicacion = publicacion("ACTIVA");
        when(publicacion.getIdAlojamiento()).thenReturn(ALOJAMIENTO);
        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(publicacion));
        when(alojamientoQuery.consultarDatosParaPublicacion(ALOJAMIENTO)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> servicio.consultar(PUBLICACION));
    }
}
