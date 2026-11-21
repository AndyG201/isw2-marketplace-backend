package co.edu.unbosque.marketplace.usuario.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;

/** Comprueba el adaptador contra un Redis real: vigencias, claves ausentes y contadores. */
@SpringBootTest
@ActiveProfiles("test")
class AlmacenTemporalRedisIntegracionTest {

    @Autowired
    private AlmacenTemporal almacen;

    private final String clave = "prueba:almacen-temporal:" + UUID.randomUUID();

    @AfterEach
    void limpiar() {
        almacen.eliminar(clave);
    }

    @Test
    void guardaYDevuelveElValorConSuVigencia() {
        almacen.guardar(clave, "valor", Duration.ofMinutes(5));

        assertEquals("valor", almacen.obtener(clave));
        Long segundos = almacen.segundosRestantes(clave);
        assertNotNull(segundos);
        assertTrue(segundos > 0 && segundos <= 300, "la vigencia debe ser de hasta 300 s y no haber expirado: " + segundos);
    }

    @Test
    void unaClaveAusenteDevuelveNulo() {
        assertNull(almacen.obtener(clave));
    }

    @Test
    void guardarReemplazaElValorAnterior() {
        almacen.guardar(clave, "uno", Duration.ofMinutes(1));
        almacen.guardar(clave, "dos", Duration.ofMinutes(1));

        assertEquals("dos", almacen.obtener(clave));
    }

    @Test
    void guardarSiAusenteSoloGuardaLaPrimeraVez() {
        assertTrue(almacen.guardarSiAusente(clave, "primero", Duration.ofSeconds(60)));
        assertFalse(almacen.guardarSiAusente(clave, "segundo", Duration.ofSeconds(60)));

        assertEquals("primero", almacen.obtener(clave));
    }

    @Test
    void eliminarBorraLaClave() {
        almacen.guardar(clave, "valor", Duration.ofMinutes(1));

        almacen.eliminar(clave);

        assertNull(almacen.obtener(clave));
    }

    @Test
    void incrementarYDecrementarLlevanUnContador() {
        assertEquals(1L, almacen.incrementar(clave));
        assertEquals(2L, almacen.incrementar(clave));
        assertEquals(1L, almacen.decrementar(clave));
        assertEquals("1", almacen.obtener(clave));
    }

    @Test
    void asignarVigenciaCambiaElTiempoRestante() {
        almacen.incrementar(clave);

        almacen.asignarVigencia(clave, Duration.ofMinutes(10));

        Long segundos = almacen.segundosRestantes(clave);
        assertNotNull(segundos);
        assertTrue(segundos > 300 && segundos <= 600, "debe quedar cerca de 600 s: " + segundos);
    }
}
