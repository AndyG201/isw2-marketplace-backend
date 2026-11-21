package co.edu.unbosque.marketplace.cancelacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.cancelacion.application.dto.PoliticaCancelacionResponse;
import co.edu.unbosque.marketplace.cancelacion.application.port.PoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;

@ExtendWith(MockitoExtension.class)
class PoliticaCancelacionServiceTest {

    @Mock
    private PoliticaCancelacionRepository politicaRepository;

    @InjectMocks
    private PoliticaCancelacionService politicaService;

    private PoliticaCancelacion crearPolitica(
            String nombre,
            int version,
            String descripcion,
            Integer anticipacion,
            BigDecimal porcentaje) {

        PoliticaCancelacion politica = new PoliticaCancelacion();
        politica.setIdPolitica(UUID.randomUUID());
        politica.setNombre(nombre);
        politica.setVersion(version);
        politica.setDescripcion(descripcion);
        politica.setAnticipacionHoras(anticipacion);
        politica.setPorcentajeDevolucion(porcentaje);
        politica.setActiva(true);

        return politica;
    }

    @Test
    void debeConsultarYRetornarPoliticasActivasPermitidas() {
        PoliticaCancelacion politica1 = crearPolitica(
                "Flexible",
                1,
                "Permite cancelaciones con devolución completa.",
                48,
                new BigDecimal("100.00")
        );

        PoliticaCancelacion politica2 = crearPolitica(
                "Moderada",
                2,
                "Permite devolución parcial.",
                24,
                new BigDecimal("50.00")
        );

        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of(politica1, politica2));

        List<PoliticaCancelacionResponse> resultado =
                politicaService.consultarPoliticasActivas();

        assertEquals(2, resultado.size());

        assertEquals(politica1.getIdPolitica(),
                resultado.get(0).getIdPolitica());
        assertEquals("Flexible", resultado.get(0).getNombre());
        assertEquals(1, resultado.get(0).getVersion());
        assertEquals(48, resultado.get(0).getAnticipacionHoras());
        assertEquals(
                0,
                new BigDecimal("100.00").compareTo(
                        resultado.get(0).getPorcentajeDevolucion()
                )
        );

        assertEquals(politica2.getIdPolitica(),
                resultado.get(1).getIdPolitica());
        assertEquals("Moderada", resultado.get(1).getNombre());

        verify(politicaRepository, times(1))
                .findByActivaTrueOrderByNombreAsc();
    }

    @Test
    void debeRetornarListaVaciaCuandoNoExistenPoliticasActivas() {
        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of());

        List<PoliticaCancelacionResponse> resultado =
                politicaService.consultarPoliticasActivas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(politicaRepository)
                .findByActivaTrueOrderByNombreAsc();
    }

    @Test
    void debeExcluirPoliticasConNombreNoPermitido() {
        PoliticaCancelacion permitida = crearPolitica(
                "Flexible",
                1,
                "Política permitida.",
                48,
                new BigDecimal("100.00")
        );

        PoliticaCancelacion noPermitida = crearPolitica(
                "Politica personalizada no permitida",
                1,
                "Política que debe excluirse.",
                12,
                new BigDecimal("25.00")
        );

        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of(permitida, noPermitida));

        List<PoliticaCancelacionResponse> resultado =
                politicaService.consultarPoliticasActivas();

        assertEquals(1, resultado.size());
        assertEquals("Flexible", resultado.get(0).getNombre());

        verify(politicaRepository)
                .findByActivaTrueOrderByNombreAsc();
    }

    @Test
    void debeExcluirTodasLasPoliticasCuandoNingunaEsPermitida() {
        PoliticaCancelacion politica1 = crearPolitica(
                "Politica personalizada A",
                1,
                "Descripción A.",
                24,
                new BigDecimal("50.00")
        );

        PoliticaCancelacion politica2 = crearPolitica(
                "Politica personalizada B",
                2,
                "Descripción B.",
                12,
                new BigDecimal("25.00")
        );

        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of(politica1, politica2));

        List<PoliticaCancelacionResponse> resultado =
                politicaService.consultarPoliticasActivas();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeConvertirTodosLosDatosDeLaEntidadEnResponse() {
        PoliticaCancelacion politica = crearPolitica(
                "Flexible",
                3,
                "Cancelación con devolución completa.",
                72,
                new BigDecimal("87.50")
        );

        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of(politica));

        List<PoliticaCancelacionResponse> resultado =
                politicaService.consultarPoliticasActivas();

        assertEquals(1, resultado.size());

        PoliticaCancelacionResponse respuesta = resultado.get(0);

        assertEquals(politica.getIdPolitica(), respuesta.getIdPolitica());
        assertEquals(politica.getNombre(), respuesta.getNombre());
        assertEquals(politica.getVersion(), respuesta.getVersion());
        assertEquals(politica.getDescripcion(), respuesta.getDescripcion());
        assertEquals(politica.getAnticipacionHoras(),
                respuesta.getAnticipacionHoras());
        assertEquals(
                0,
                politica.getPorcentajeDevolucion().compareTo(
                        respuesta.getPorcentajeDevolucion()
                )
        );
    }

    @Test
    void debeRetornarListaInmodificable() {
        PoliticaCancelacion politica = crearPolitica(
                "Flexible",
                1,
                "Descripción.",
                48,
                new BigDecimal("100.00")
        );

        when(politicaRepository.findByActivaTrueOrderByNombreAsc())
                .thenReturn(List.of(politica));

        List<PoliticaCancelacionResponse> resultado =
                politicaService.consultarPoliticasActivas();

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.add(
                        new PoliticaCancelacionResponse(
                                UUID.randomUUID(),
                                "Otra política",
                                1,
                                "Descripción.",
                                24,
                                BigDecimal.ZERO
                        )
                )
        );
    }
}
