package co.edu.unbosque.marketplace.doble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;

/** Comportamiento que toda implementación del almacén temporal debe cumplir (Redis y en memoria). */
abstract class ContratoAlmacenTemporalTest {

    private final String clave = "contrato:almacen:" + UUID.randomUUID();

    abstract AlmacenTemporal almacen();

    @AfterEach
    void limpiar() {
        almacen().eliminar(clave);
    }

    @Test
    void guardaYDevuelveElValorYUnaClaveAusenteDevuelveNulo() {
        assertNull(almacen().obtener(clave));

        almacen().guardar(clave, "valor", Duration.ofMinutes(5));

        assertEquals("valor", almacen().obtener(clave));
    }

    @Test
    void guardarReemplazaYEliminarBorra() {
        almacen().guardar(clave, "uno", Duration.ofMinutes(1));
        almacen().guardar(clave, "dos", Duration.ofMinutes(1));
        assertEquals("dos", almacen().obtener(clave));

        almacen().eliminar(clave);

        assertNull(almacen().obtener(clave));
    }

    @Test
    void guardarSiAusenteSoloGuardaLaPrimeraVez() {
        assertTrue(almacen().guardarSiAusente(clave, "primero", Duration.ofMinutes(1)));
        assertFalse(almacen().guardarSiAusente(clave, "segundo", Duration.ofMinutes(1)));

        assertEquals("primero", almacen().obtener(clave));
    }

    @Test
    void incrementarYDecrementarLlevanUnContador() {
        assertEquals(1L, almacen().incrementar(clave));
        assertEquals(2L, almacen().incrementar(clave));
        assertEquals(1L, almacen().decrementar(clave));
        assertEquals("1", almacen().obtener(clave));
    }

    @Test
    void laVigenciaRestanteSeConsultaYSePuedeCambiar() {
        almacen().guardar(clave, "valor", Duration.ofMinutes(5));
        Long restante = almacen().segundosRestantes(clave);
        assertNotNull(restante);
        assertTrue(restante > 0 && restante <= 300, "restante: " + restante);

        almacen().asignarVigencia(clave, Duration.ofMinutes(10));

        restante = almacen().segundosRestantes(clave);
        assertTrue(restante > 300 && restante <= 600, "restante: " + restante);
    }

    @Test
    void unaClaveAusenteNoTieneVigencia() {
        assertNull(almacen().segundosRestantes(clave));
    }

    @Test
    void cambiarUnContadorNoAlteraSuVigencia() {
        almacen().incrementar(clave);
        almacen().asignarVigencia(clave, Duration.ofMinutes(10));

        almacen().incrementar(clave);

        Long restante = almacen().segundosRestantes(clave);
        assertNotNull(restante);
        assertTrue(restante > 300 && restante <= 600, "restante: " + restante);
    }
}
