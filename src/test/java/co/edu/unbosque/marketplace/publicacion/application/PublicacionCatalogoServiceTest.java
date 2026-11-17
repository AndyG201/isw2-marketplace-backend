package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicacionCatalogoServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private AlojamientoPublicacionQuery alojamientoQuery;

    @InjectMocks
    private PublicacionCatalogoService service;

    private UUID idPublicacion;
    private UUID idAlojamiento;
    private UUID idPublicacion2;
    private UUID idAlojamiento2;

    private Publicacion publicacion;
    private Publicacion publicacion2;

    private AlojamientoPublicacionInfo alojamientoInfo;
    private AlojamientoPublicacionInfo alojamientoInfo2;

    @BeforeEach
    void setUp() {

        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();

        idPublicacion2 = UUID.randomUUID();
        idAlojamiento2 = UUID.randomUUID();

        publicacion = mock(Publicacion.class);
        publicacion2 = mock(Publicacion.class);

        alojamientoInfo = new AlojamientoPublicacionInfo(
                idAlojamiento,
                idAlojamiento, "Bogotá",
                "Apartamento",
                null, 4
        );

        alojamientoInfo2 = new AlojamientoPublicacionInfo(
                idAlojamiento2,
                idAlojamiento, "Medellín",
                "Casa",
                null, 6
        );

        when(publicacion.getIdPublicacion()).thenReturn(idPublicacion);
        when(publicacion.getIdAlojamiento()).thenReturn(idAlojamiento);
        when(publicacion.getTitulo()).thenReturn("Apartamento moderno");
        when(publicacion.getPrecioPorNoche()).thenReturn(new BigDecimal("180000"));
        when(publicacion.getEstado()).thenReturn("ACTIVA");
        when(publicacion.getFechaCreacion()).thenReturn(OffsetDateTime.now());

        when(publicacion2.getIdPublicacion()).thenReturn(idPublicacion2);
        when(publicacion2.getIdAlojamiento()).thenReturn(idAlojamiento2);
        when(publicacion2.getTitulo()).thenReturn("Casa familiar");
        when(publicacion2.getPrecioPorNoche()).thenReturn(new BigDecimal("250000"));
        when(publicacion2.getEstado()).thenReturn("ACTIVA");
        when(publicacion2.getFechaCreacion()).thenReturn(OffsetDateTime.now());
    }

    @Test
    void consultarCatalogoCorrectamente() {

        when(publicacionRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA"))
                .thenReturn(List.of(publicacion, publicacion2));

        when(alojamientoQuery.consultarPorIdsAlojamiento(anyList()))
                .thenReturn(List.of(alojamientoInfo, alojamientoInfo2));

        List<PublicacionResumenResponse> resultado =
                service.consultarCatalogo();

        assertEquals(2, resultado.size());

        verify(publicacionRepository)
                .findByEstadoOrderByFechaCreacionDesc("ACTIVA");

        verify(alojamientoQuery)
                .consultarPorIdsAlojamiento(anyList());
    }

    @Test
    void consultarCatalogoSinPublicaciones() {

        when(publicacionRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA"))
                .thenReturn(List.of());

        List<PublicacionResumenResponse> resultado =
                service.consultarCatalogo();

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(alojamientoQuery);
    }

    @Test
    void consultarCatalogoConUnaPublicacion() {

        when(publicacionRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA"))
                .thenReturn(List.of(publicacion));

        when(alojamientoQuery.consultarPorIdsAlojamiento(anyList()))
                .thenReturn(List.of(alojamientoInfo));

        List<PublicacionResumenResponse> resultado =
                service.consultarCatalogo();

        assertEquals(1, resultado.size());

        verify(alojamientoQuery)
                .consultarPorIdsAlojamiento(anyList());
    }

    @Test
    void consultarCatalogoConAlojamientosRepetidos() {

        when(publicacion2.getIdAlojamiento()).thenReturn(idAlojamiento);

        when(publicacionRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA"))
                .thenReturn(List.of(publicacion, publicacion2));

        when(alojamientoQuery.consultarPorIdsAlojamiento(anyList()))
                .thenReturn(List.of(alojamientoInfo));

        List<PublicacionResumenResponse> resultado =
                service.consultarCatalogo();

        assertEquals(2, resultado.size());

        verify(alojamientoQuery).consultarPorIdsAlojamiento(
                argThat(ids -> ids.size() == 1 && ids.contains(idAlojamiento))
        );
    }

    @Test
    void consultarCatalogoSinAlojamientoAsociado() {

        when(publicacionRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA"))
                .thenReturn(List.of(publicacion));

        when(alojamientoQuery.consultarPorIdsAlojamiento(anyList()))
                .thenReturn(List.of());

        assertThrows(
                IllegalStateException.class,
                () -> service.consultarCatalogo()
        );

        verify(alojamientoQuery)
                .consultarPorIdsAlojamiento(anyList());
    }

    @Test
    void consultarCatalogoConUnAlojamientoFaltante() {

        when(publicacionRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA"))
                .thenReturn(List.of(publicacion, publicacion2));

        when(alojamientoQuery.consultarPorIdsAlojamiento(anyList()))
                .thenReturn(List.of(alojamientoInfo));

        assertThrows(
                IllegalStateException.class,
                () -> service.consultarCatalogo()
        );
    }

    @Test
    void consultarCatalogoConsultaSoloPublicacionesActivas() {

        when(publicacionRepository.findByEstadoOrderByFechaCreacionDesc("ACTIVA"))
                .thenReturn(List.of());

        service.consultarCatalogo();

        verify(publicacionRepository)
                .findByEstadoOrderByFechaCreacionDesc("ACTIVA");

        verify(publicacionRepository, never())
                .findByEstadoOrderByFechaCreacionDesc("BORRADOR");
    }
}
