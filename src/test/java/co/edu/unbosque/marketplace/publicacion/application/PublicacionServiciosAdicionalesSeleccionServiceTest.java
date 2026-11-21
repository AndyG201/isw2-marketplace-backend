package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import co.edu.unbosque.marketplace.publicacion.api.response.ServicioAdicionalPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioAdicionalQuery;

@ExtendWith(MockitoExtension.class)
class PublicacionServiciosAdicionalesSeleccionServiceTest {

    private static final UUID PUBLICACION = UUID.randomUUID();
    private static final UUID ADICIONAL = UUID.randomUUID();

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private ServicioAdicionalQuery servicioAdicionalQuery;

    private PublicacionServiciosAdicionalesSeleccionService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PublicacionServiciosAdicionalesSeleccionService(
                publicacionRepository, servicioAdicionalQuery);
    }

    private void registrarDesayuno() {
        when(servicioAdicionalQuery.consultarActivosPorPublicacion(PUBLICACION)).thenReturn(List.of(
                new ServicioAdicionalInfo(
                        ADICIONAL, PUBLICACION, "Desayuno", "Típico", new BigDecimal("25000"), true)));
    }

    @Test
    void consultaLosServiciosRegistradosSinImportarElEstadoDeLaPublicacion() {
        registrarDesayuno();

        List<ServicioAdicionalPublicacionResponse> servicios = servicio.consultarRegistrados(PUBLICACION);

        assertEquals(1, servicios.size());
        assertEquals(ADICIONAL, servicios.get(0).getIdAdicional());
        assertEquals("Desayuno", servicios.get(0).getNombre());
        verifyNoInteractions(publicacionRepository);
    }

    @Test
    void enElMarketplaceSoloMuestraLosServiciosDePublicacionesActivas() {
        Publicacion activa = mock(Publicacion.class);
        when(activa.getEstado()).thenReturn("ACTIVA");
        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(activa));
        registrarDesayuno();

        assertEquals(1, servicio.consultarDisponibles(PUBLICACION).size());
    }

    @Test
    void noMuestraLosServiciosDeUnaPublicacionQueNoEstaActiva() {
        Publicacion enRevision = mock(Publicacion.class);
        when(enRevision.getEstado()).thenReturn("PENDIENTE_REVISION");
        when(publicacionRepository.findById(PUBLICACION)).thenReturn(Optional.of(enRevision));

        assertThrows(NoSuchElementException.class, () -> servicio.consultarDisponibles(PUBLICACION));
        verifyNoInteractions(servicioAdicionalQuery);
    }
}
