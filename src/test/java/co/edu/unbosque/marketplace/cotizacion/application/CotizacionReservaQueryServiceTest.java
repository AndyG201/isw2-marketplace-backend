package co.edu.unbosque.marketplace.cotizacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaInfo;
import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;
import co.edu.unbosque.marketplace.cotizacion.infrastructure.persistence.CotizacionRepository;

class CotizacionReservaQueryServiceTest {

    private CotizacionRepository cotizacionRepository;
    private CotizacionReservaQueryService service;

    @BeforeEach
    void setUp() {
        cotizacionRepository = mock(CotizacionRepository.class);
        service = new CotizacionReservaQueryService(cotizacionRepository);
    }

    @Test
    void consultarPorPublicacionRetornaListaDeCotizaciones() {
        UUID idPublicacion = UUID.randomUUID();

        Cotizacion cotizacion = crearCotizacion(idPublicacion);

        when(cotizacionRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(cotizacion));

        List<CotizacionReservaInfo> resultado =
                service.consultarPorPublicacion(idPublicacion);

        assertEquals(1, resultado.size());
        assertEquals(cotizacion.getIdCotizacion(),
                resultado.get(0).idCotizacion());
        assertEquals(idPublicacion,
                resultado.get(0).idPublicacion());
        assertEquals(cotizacion.getFechaEntrada(),
                resultado.get(0).fechaEntrada());
        assertEquals(cotizacion.getFechaSalida(),
                resultado.get(0).fechaSalida());

        verify(cotizacionRepository).findByIdPublicacion(idPublicacion);
    }

    @Test
    void consultarPorPublicacionConIdNuloRetornaListaVacia() {
        List<CotizacionReservaInfo> resultado =
                service.consultarPorPublicacion(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(cotizacionRepository);
    }

    @Test
    void consultarPorPublicacionSinResultadosRetornaListaVacia() {
        UUID idPublicacion = UUID.randomUUID();

        when(cotizacionRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of());

        List<CotizacionReservaInfo> resultado =
                service.consultarPorPublicacion(idPublicacion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(cotizacionRepository).findByIdPublicacion(idPublicacion);
    }

    @Test
    void consultarPorPublicacionConvierteTodasLasCotizaciones() {
        UUID idPublicacion = UUID.randomUUID();

        Cotizacion primera = crearCotizacion(idPublicacion);
        Cotizacion segunda = crearCotizacion(idPublicacion);

        when(cotizacionRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(primera, segunda));

        List<CotizacionReservaInfo> resultado =
                service.consultarPorPublicacion(idPublicacion);

        assertEquals(2, resultado.size());
        assertEquals(primera.getIdCotizacion(),
                resultado.get(0).idCotizacion());
        assertEquals(segunda.getIdCotizacion(),
                resultado.get(1).idCotizacion());
    }

    private Cotizacion crearCotizacion(UUID idPublicacion) {
        Cotizacion cotizacion = new Cotizacion();

        cotizacion.setIdCotizacion(UUID.randomUUID());
        cotizacion.setIdPublicacion(idPublicacion);
        cotizacion.setFechaEntrada(LocalDate.of(2026, 12, 10));
        cotizacion.setFechaSalida(LocalDate.of(2026, 12, 15));

        return cotizacion;
    }
}
