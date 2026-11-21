
package co.edu.unbosque.marketplace.cancelacion.application;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;

class DevolucionEstrictaStrategyTest {

    private DevolucionEstrictaStrategy estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new DevolucionEstrictaStrategy();
    }

    @Test
    void reconocePoliticaEstricta() {
        assertTrue(estrategia.aplicaA("ESTRICTA"));
        assertTrue(estrategia.aplicaA(" estricta "));
        assertTrue(estrategia.aplicaA("Estricta"));
    }

    @Test
    void rechazaPoliticasDiferentes() {
        assertFalse(estrategia.aplicaA("FLEXIBLE"));
        assertFalse(estrategia.aplicaA("MODERADA"));
        assertFalse(estrategia.aplicaA(null));
    }

    @Test
    void noRealizaDevolucionAunqueExistaAnticipacion() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("500000"),
                politica(),
                30
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void noRealizaDevolucionCuandoSeCancelaElMismoDia() {

        BigDecimal resultado = estrategia.calcular(
                new BigDecimal("100000"),
                politica(),
                0
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void devuelveCeroAunqueLosParametrosSeanNulos() {

        BigDecimal resultado = estrategia.calcular(
                null,
                null,
                -1
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    private PoliticaCancelacionInfo politica() {

        return new PoliticaCancelacionInfo(
                UUID.randomUUID(),
                "ESTRICTA",
                1,
                "Política estricta de prueba",
                24,
                new BigDecimal("100"),
                true
        );
    }
}
