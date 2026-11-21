
package co.edu.unbosque.marketplace.cancelacion.application;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
class DevolucionModeradaStrategyTest {

    private DevolucionModeradaStrategy estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new DevolucionModeradaStrategy();
    }

    @Test
    void reconocePoliticaModerada() {
        assertTrue(estrategia.aplicaA("MODERADA"));
        assertTrue(estrategia.aplicaA(" moderada "));
        assertTrue(estrategia.aplicaA("Moderada"));
    }

    @Test
    void rechazaPoliticasDiferentes() {
        assertFalse(estrategia.aplicaA("FLEXIBLE"));
        assertFalse(estrategia.aplicaA("ESTRICTA"));
        assertFalse(estrategia.aplicaA(null));
    }

    @Test
    void calculaDevolucionCorrectamente() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("200000"),
                politica(120, "60"),
                5
        );

        assertEquals(new BigDecimal("120000.00"), resultado);
    }

    @Test
    void noDevuelveDineroCuandoNoCumpleAnticipacion() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("200000"),
                politica(120, "60"),
                4
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void redondeaAnticipacionDeHorasADias() {

        PoliticaCancelacionInfo politica = politica(49, "50");

        BigDecimal dosDias = estrategia.calcular(
                new BigDecimal("100000"), politica, 2
        );

        BigDecimal tresDias = estrategia.calcular(
                new BigDecimal("100000"), politica, 3
        );

        assertAll(
                () -> assertEquals(new BigDecimal("0.00"), dosDias),
                () -> assertEquals(new BigDecimal("50000.00"), tresDias)
        );
    }

    @Test
    void utilizaAnticipacionPredeterminadaDe120Horas() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(null, "70"),
                5
        );

        assertEquals(new BigDecimal("70000.00"), resultado);
    }

    @Test
    void rechazaMontosNulosOCeroONegativos() {

        PoliticaCancelacionInfo politica = politica(120, "60");

        assertAll(
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        estrategia.calcular(null, politica, 10)
                ),
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        estrategia.calcular(BigDecimal.ZERO, politica, 10)
                ),
                () -> assertEquals(
                        new BigDecimal("0.00"),
                        estrategia.calcular(
                                new BigDecimal("-200"), politica, 10
                        )
                )
        );
    }

    @Test
    void devuelveCeroCuandoElPorcentajeEsNulo() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(120, null),
                10
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void limitaPorcentajeMaximoAlCien() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(120, "130"),
                10
        );

        assertEquals(new BigDecimal("100000.00"), resultado);
    }

    @Test
    void limitaPorcentajeMinimoACero() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(120, "-10"),
                10
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void redondeaElResultadoMonetario() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("10.00"),
                politica(120, "33.35"),
                10
        );

        assertEquals(new BigDecimal("3.34"), resultado);
    }

    private PoliticaCancelacionInfo politica(
            Integer anticipacion,
            String porcentaje) {

        return new PoliticaCancelacionInfo(
                UUID.randomUUID(),
                "MODERADA",
                1,
                "Política moderada de prueba",
                anticipacion,
                porcentaje == null ? null : new BigDecimal(porcentaje),
                true
        );
    }
}
