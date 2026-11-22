package co.edu.unbosque.marketplace.doble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajos.Trabajo;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

/** Comportamiento que toda implementación de la cola de trabajos debe cumplir (Redis y en memoria). */
abstract class ContratoColaTrabajosTest {

    abstract ColaTrabajos cola();

    @BeforeEach
    void vaciar() {
        while (cola().tomarSiguiente().isPresent()) {
            // Se descarta lo que haya quedado de otras pruebas.
        }
    }

    @Test
    void unaColaVaciaNoDevuelveNada() {
        assertTrue(cola().tomarSiguiente().isEmpty());
    }

    @Test
    void devuelveElTrabajoEncoladoYLuegoQuedaVacia() {
        cola().encolar(TipoTrabajo.CONCILIAR_PAGOS, "pendientes");

        assertEquals(new Trabajo(TipoTrabajo.CONCILIAR_PAGOS, "pendientes"), cola().tomarSiguiente().orElseThrow());
        assertTrue(cola().tomarSiguiente().isEmpty());
    }

    @Test
    void sacaPrimeroLaMayorPrioridadSinImportarElOrdenDeLlegada() {
        cola().encolar(TipoTrabajo.EXPORTAR_REPORTE, "reporte");
        cola().encolar(TipoTrabajo.COPIAR_AUDITORIA, "auditoria");
        cola().encolar(TipoTrabajo.CONCILIAR_PAGOS, "pagos");

        assertEquals(TipoTrabajo.CONCILIAR_PAGOS, cola().tomarSiguiente().orElseThrow().tipo());
        assertEquals(TipoTrabajo.COPIAR_AUDITORIA, cola().tomarSiguiente().orElseThrow().tipo());
        assertEquals(TipoTrabajo.EXPORTAR_REPORTE, cola().tomarSiguiente().orElseThrow().tipo());
    }

    @Test
    void conLaMismaPrioridadSaleElMasAntiguo() throws InterruptedException {
        cola().encolar(TipoTrabajo.EXPORTAR_REPORTE, "primero");
        Thread.sleep(5);
        cola().encolar(TipoTrabajo.EXPORTAR_REPORTE, "segundo");

        assertEquals("primero", cola().tomarSiguiente().orElseThrow().identificador());
        assertEquals("segundo", cola().tomarSiguiente().orElseThrow().identificador());
    }

    @Test
    void unTrabajoYaPendienteNoSeEncolaDosVeces() {
        cola().encolar(TipoTrabajo.EXPIRAR_RETENCIONES, "barrido");
        cola().encolar(TipoTrabajo.EXPIRAR_RETENCIONES, "barrido");

        assertTrue(cola().tomarSiguiente().isPresent());
        assertTrue(cola().tomarSiguiente().isEmpty());
    }

    @Test
    void despuesDeTomarloElMismoTrabajoSePuedeEncolarDeNuevo() {
        cola().encolar(TipoTrabajo.EXPIRAR_RETENCIONES, "barrido");
        cola().tomarSiguiente();

        cola().encolar(TipoTrabajo.EXPIRAR_RETENCIONES, "barrido");

        assertTrue(cola().tomarSiguiente().isPresent());
    }
}
