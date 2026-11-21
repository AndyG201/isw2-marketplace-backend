package co.edu.unbosque.marketplace.cancelacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.cancelacion.application.port.PoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;

@ExtendWith(MockitoExtension.class)
class PoliticaCancelacionQueryServiceTest {

    @Mock
    private PoliticaCancelacionRepository politicaRepository;

    @InjectMocks
    private PoliticaCancelacionQueryService politicaService;

    private UUID idPolitica;
    private PoliticaCancelacion politica;

    @BeforeEach
    void setUp() {
        idPolitica = UUID.randomUUID();

        politica = new PoliticaCancelacion();
        politica.setIdPolitica(idPolitica);
        politica.setNombre("Flexible");
        politica.setVersion(1);
        politica.setDescripcion("Politica de cancelacion flexible");
        politica.setAnticipacionHoras(48);
        politica.setPorcentajeDevolucion(new BigDecimal("100.00"));
        politica.setActiva(true);
    }

    @Test
    void existePoliticaActivaCuandoLaPoliticaEsSeleccionable() {
        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(politica));

        assertTrue(politicaService.existePoliticaActiva(idPolitica));

        verify(politicaRepository).findByIdPoliticaAndActivaTrue(idPolitica);
    }

    @Test
    void existePoliticaActivaCuandoElIdEsNulo() {
        assertFalse(politicaService.existePoliticaActiva(null));

        verifyNoInteractions(politicaRepository);
    }

    @Test
    void consultarPoliticaSeleccionableRetornaPoliticaCuandoEsValida() {
        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(politica));

        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPoliticaSeleccionable(idPolitica);

        assertTrue(resultado.isPresent());
        assertEquals(idPolitica, resultado.get().idPolitica());
        assertEquals("Flexible", resultado.get().nombre());
        assertEquals(1, resultado.get().version());
        assertEquals("Politica de cancelacion flexible", resultado.get().descripcion());
        assertEquals(48, resultado.get().anticipacionHoras());
        assertEquals(0, new BigDecimal("100.00")
                .compareTo(resultado.get().porcentajeDevolucion()));
        assertTrue(resultado.get().activa());
    }

    @Test
    void consultarPoliticaSeleccionableRetornaVacioCuandoElIdEsNulo() {
        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPoliticaSeleccionable(null);

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(politicaRepository);
    }

    @Test
    void consultarPoliticaSeleccionableRetornaVacioCuandoNoExiste() {
        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.empty());

        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPoliticaSeleccionable(idPolitica);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarPoliticaSeleccionableRetornaVacioCuandoElNombreNoEsPermitido() {
        politica.setNombre("Politica personalizada no permitida");

        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(politica));

        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPoliticaSeleccionable(idPolitica);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarPorIdRetornaPoliticaCuandoExiste() {
        when(politicaRepository.findById(idPolitica))
                .thenReturn(Optional.of(politica));

        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPorId(idPolitica);

        assertTrue(resultado.isPresent());
        assertEquals(idPolitica, resultado.get().idPolitica());
        assertEquals("Flexible", resultado.get().nombre());
        assertEquals(1, resultado.get().version());
        assertEquals("Politica de cancelacion flexible", resultado.get().descripcion());
        assertEquals(48, resultado.get().anticipacionHoras());
        assertEquals(0, new BigDecimal("100.00")
                .compareTo(resultado.get().porcentajeDevolucion()));
        assertTrue(resultado.get().activa());
    }

    @Test
    void consultarPorIdRetornaVacioCuandoElIdEsNulo() {
        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPorId(null);

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(politicaRepository);
    }

    @Test
    void consultarPorIdRetornaVacioCuandoNoExiste() {
        when(politicaRepository.findById(idPolitica))
                .thenReturn(Optional.empty());

        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPorId(idPolitica);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void consultarPorIdRetornaPoliticaAunqueEsteInactiva() {
        politica.setActiva(false);

        when(politicaRepository.findById(idPolitica))
                .thenReturn(Optional.of(politica));

        Optional<PoliticaCancelacionInfo> resultado =
                politicaService.consultarPorId(idPolitica);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().activa());
    }
}

