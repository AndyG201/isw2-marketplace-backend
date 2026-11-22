package co.edu.unbosque.marketplace.doble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;

class AlmacenTemporalEnMemoriaContratoTest extends ContratoAlmacenTemporalTest {

    private final AlmacenTemporal almacen = new AlmacenTemporalEnMemoria();

    @Override
    AlmacenTemporal almacen() {
        return almacen;
    }

    @Test
    void losValoresExpiranConElReloj() {
        AtomicReference<Instant> ahora = new AtomicReference<>(Instant.parse("2030-01-01T00:00:00Z"));
        AlmacenTemporalEnMemoria conReloj = new AlmacenTemporalEnMemoria(ahora::get);
        conReloj.guardar("k", "v", Duration.ofSeconds(30));
        assertEquals(30L, conReloj.segundosRestantes("k"));

        ahora.set(ahora.get().plusSeconds(29));
        assertEquals("v", conReloj.obtener("k"));
        assertFalse(conReloj.guardarSiAusente("k", "otro", Duration.ofSeconds(30)));

        ahora.set(ahora.get().plusSeconds(1));
        assertNull(conReloj.obtener("k"));
        assertNull(conReloj.segundosRestantes("k"));
        assertTrue(conReloj.guardarSiAusente("k", "otro", Duration.ofSeconds(30)));
        // Asignar vigencia a una clave ausente no la crea.
        conReloj.asignarVigencia("fantasma", Duration.ofSeconds(5));
        assertNull(conReloj.obtener("fantasma"));
    }
}
