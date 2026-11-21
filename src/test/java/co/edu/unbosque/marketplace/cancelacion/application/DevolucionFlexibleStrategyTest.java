package co.edu.unbosque.marketplace.cancelacion.application;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;

class DevolucionFlexibleStrategyTest {

    private DevolucionFlexibleStrategy estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new DevolucionFlexibleStrategy();
    }

    @Test
    void reconocePoliticaFlexible() {
        assertTrue(estrategia.aplicaA("FLEXIBLE"));
        assertTrue(estrategia.aplicaA(" flexible "));
        assertTrue(estrategia.aplicaA("Flexible"));
    }

    @Test
    void rechazaPoliticasDiferentes() {
        assertFalse(estrategia.aplicaA("ESTRICTA"));
        assertFalse(estrategia.aplicaA("MODERADA"));
        assertFalse(estrategia.aplicaA(null));
    }

    @Test
    void calculaDevolucionCorrectamente() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(24, "80"),
                1
        );

        assertEquals(new BigDecimal("80000.00"), resultado);
    }

    @Test
    void noDevuelveDineroCuandoNoCumpleAnticipacion() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(24, "80"),
                0
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void redondeaAnticipacionDeHorasADias() {

        PoliticaCancelacionInfo politica = politica(25, "80");

        BigDecimal unDia = estrategia.calcular(
                new BigDecimal("100000"), politica, 1
        );

        BigDecimal dosDias = estrategia.calcular(
                new BigDecimal("100000"), politica, 2
        );

        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), unDia),
                () -> assertEquals(new BigDecimal("80000.00"), dosDias)
        );
    }

    @Test
    void utilizaAnticipacionPredeterminadaDe24Horas() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(null, "50"),
                1
        );

        assertEquals(new BigDecimal("50000.00"), resultado);
    }

    @Test
    void rechazaMontosNulosOCeroONegativos() {

        PoliticaCancelacionInfo politica = politica(24, "80");

        assertAll(
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        estrategia.calcular(null, politica, 5)
                ),
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        estrategia.calcular(BigDecimal.ZERO, politica, 5)
                ),
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        estrategia.calcular(
                                new BigDecimal("-100"), politica, 5
                        )
                )
        );
    }

    @Test
    void devuelveCeroCuandoElPorcentajeEsNulo() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(24, null),
                5
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void limitaPorcentajeMaximoAlCien() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(24, "150"),
                5
        );

        assertEquals(new BigDecimal("100000.00"), resultado);
    }

    @Test
    void limitaPorcentajeMinimoACero() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(24, "-20"),
                5
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void redondeaElResultadoMonetario() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("10.00"),
                politica(24, "33.35"),
                5
        );

        assertEquals(new BigDecimal("3.34"), resultado);
    }

    private PoliticaCancelacionInfo politica(
            Integer anticipacion,
            String porcentaje) {

        return new PoliticaCancelacionInfo(
                UUID.randomUUID(),
                "FLEXIBLE",
                1,
                "Política flexible de prueba",
                anticipacion,
                porcentaje == null ? null : new BigDecimal(porcentaje),
                true
        );
    }
}
