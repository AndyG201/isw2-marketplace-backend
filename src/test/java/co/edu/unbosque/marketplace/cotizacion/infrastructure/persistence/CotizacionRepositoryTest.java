package co.edu.unbosque.marketplace.cotizacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.cotizacion.domain.Cotizacion;

class CotizacionRepositoryTest {

    private CotizacionRepository cotizacionRepository;

    @BeforeEach
    void setUp() {
        cotizacionRepository = mock(CotizacionRepository.class);
    }

    @Test
    void findByIdPublicacionRetornaCotizaciones() {
        UUID idPublicacion = UUID.randomUUID();

        Cotizacion cotizacion = crearCotizacion(idPublicacion);

        when(cotizacionRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(cotizacion));

        List<Cotizacion> resultado =
                cotizacionRepository.findByIdPublicacion(idPublicacion);

        assertEquals(1, resultado.size());
        assertEquals(idPublicacion,
                resultado.get(0).getIdPublicacion());

        verify(cotizacionRepository).findByIdPublicacion(idPublicacion);
    }

    @Test
    void findByIdPublicacionRetornaListaVacia() {
        UUID idPublicacion = UUID.randomUUID();

        when(cotizacionRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of());

        List<Cotizacion> resultado =
                cotizacionRepository.findByIdPublicacion(idPublicacion);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(cotizacionRepository).findByIdPublicacion(idPublicacion);
    }

    @Test
    void findByIdPublicacionRetornaVariasCotizaciones() {
        UUID idPublicacion = UUID.randomUUID();

        Cotizacion primera = crearCotizacion(idPublicacion);
        Cotizacion segunda = crearCotizacion(idPublicacion);

        when(cotizacionRepository.findByIdPublicacion(idPublicacion))
                .thenReturn(List.of(primera, segunda));

        List<Cotizacion> resultado =
                cotizacionRepository.findByIdPublicacion(idPublicacion);

        assertEquals(2, resultado.size());
        assertEquals(primera.getIdCotizacion(),
                resultado.get(0).getIdCotizacion());
        assertEquals(segunda.getIdCotizacion(),
                resultado.get(1).getIdCotizacion());

        verify(cotizacionRepository).findByIdPublicacion(idPublicacion);
    }

    private Cotizacion crearCotizacion(UUID idPublicacion) {
        return new Cotizacion(
                UUID.randomUUID(),
                idPublicacion,
                UUID.randomUUID(),
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                2,
                new BigDecimal("150000.00"),
                new BigDecimal("50000.00"),
                OffsetDateTime.now()
        );
    }
}
