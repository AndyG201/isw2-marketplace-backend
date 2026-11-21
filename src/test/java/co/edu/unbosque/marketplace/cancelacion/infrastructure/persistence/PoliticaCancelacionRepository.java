package co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;

class PoliticaCancelacionRepositoryTest {

    private PoliticaCancelacionRepository politicaRepository;

    private UUID idPolitica;
    private PoliticaCancelacion politica;

    @BeforeEach
    void setUp() {
        politicaRepository = mock(PoliticaCancelacionRepository.class);

        idPolitica = UUID.randomUUID();

        politica = new PoliticaCancelacion();
        politica.setIdPolitica(idPolitica);
        politica.setNombre("FLEXIBLE");
        politica.setVersion(1);
        politica.setDescripcion("Politica flexible");
        politica.setAnticipacionHoras(48);
        politica.setPorcentajeDevolucion(
                new java.math.BigDecimal("100.00"));
        politica.setActiva(true);
    }

    @Test
    void findByIdPoliticaAndActivaTrueRetornaPolitica() {
        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.of(politica));

        Optional<PoliticaCancelacion> resultado =
                politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica);

        assertTrue(resultado.isPresent());
        assertEquals(politica, resultado.get());

        verify(politicaRepository)
                .findByIdPoliticaAndActivaTrue(idPolitica);
    }

    @Test
    void findByIdPoliticaAndActivaTrueRetornaVacio() {
        when(politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica))
                .thenReturn(Optional.empty());

        Optional<PoliticaCancelacion> resultado =
                politicaRepository.findByIdPoliticaAndActivaTrue(idPolitica);

        assertTrue(resultado.isEmpty());

        verify(politicaRepository)
                .findByIdPoliticaAndActivaTrue(idPolitica);
    }

    @Test
    void findByActivaTrueOrderByNombreAscRetornaPoliticas() {
        PoliticaCancelacion politicaModerada = new PoliticaCancelacion();
        politicaModerada.setNombre("MODERADA");

        List<PoliticaCancelacion> politicas =
                List.of(politica, politicaModerada);

        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(politicas);

        List<PoliticaCancelacion> resultado =
                politicaRepository.findByActivaTrueOrderByNombreAsc();

        assertEquals(2, resultado.size());
        assertEquals(politicas, resultado);

        verify(politicaRepository)
                .findByActivaTrueOrderByNombreAsc();
    }

    @Test
    void findByActivaTrueOrderByNombreAscRetornaListaVacia() {
        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of());

        List<PoliticaCancelacion> resultado =
                politicaRepository.findByActivaTrueOrderByNombreAsc();

        assertTrue(resultado.isEmpty());

        verify(politicaRepository)
                .findByActivaTrueOrderByNombreAsc();
    }
}
