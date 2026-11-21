package co.edu.unbosque.marketplace.cancelacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TipoPoliticaCancelacionTest {

    @Test
    void flexibleEsPermitida() {
        assertTrue(TipoPoliticaCancelacion.esPermitida("FLEXIBLE"));
    }

    @Test
    void moderadaEsPermitida() {
        assertTrue(TipoPoliticaCancelacion.esPermitida("MODERADA"));
    }

    @Test
    void estrictaEsPermitida() {
        assertTrue(TipoPoliticaCancelacion.esPermitida("ESTRICTA"));
    }

    @Test
    void nombreEnMinusculasEsPermitido() {
        assertTrue(TipoPoliticaCancelacion.esPermitida("flexible"));
    }

    @Test
    void nombreConEspaciosEsPermitido() {
        assertTrue(TipoPoliticaCancelacion.esPermitida("  MODERADA  "));
    }

    @Test
    void nombreNuloNoEsPermitido() {
        assertFalse(TipoPoliticaCancelacion.esPermitida(null));
    }

    @Test
    void nombreVacioNoEsPermitido() {
        assertFalse(TipoPoliticaCancelacion.esPermitida(""));
    }

    @Test
    void nombreConEspaciosEnBlancoNoEsPermitido() {
        assertFalse(TipoPoliticaCancelacion.esPermitida("   "));
    }

    @Test
    void nombreInexistenteNoEsPermitido() {
        assertFalse(TipoPoliticaCancelacion.esPermitida("PERSONALIZADA"));
    }

    @Test
    void nombreParcialNoEsPermitido() {
        assertFalse(TipoPoliticaCancelacion.esPermitida("FLEX"));
    }
}
