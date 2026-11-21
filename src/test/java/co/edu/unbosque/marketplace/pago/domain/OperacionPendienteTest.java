package co.edu.unbosque.marketplace.pago.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class OperacionPendienteTest {

    @Test
    void constructorVacioDebeCrearObjeto() {
        OperacionPendiente operacion = new OperacionPendiente();

        assertNotNull(operacion);
        assertNull(operacion.getIdOperacion());
        assertNull(operacion.getIdReserva());
        assertNull(operacion.getIdPago());
        assertNull(operacion.getTipoOperacion());
        assertNull(operacion.getEstado());
        assertEquals(0, operacion.getIntentos());
        assertNull(operacion.getProximaEjecucion());
        assertNull(operacion.getUltimaEjecucion());
        assertNull(operacion.getUltimoError());
        assertNull(operacion.getClaveIdempotencia());
        assertNull(operacion.getFechaCreacion());
        assertNull(operacion.getFechaActualizacion());
    }

    @Test
    void constructorCompletoDebeAsignarTodosLosAtributos() {
        UUID idOperacion = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        UUID idPago = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime proxima = ahora.plusHours(1);
        OffsetDateTime ultima = ahora.minusHours(1);

        OperacionPendiente operacion = new OperacionPendiente(
                idOperacion, idReserva, idPago, "CONFIRMAR_PAGO",
                "PENDIENTE", 2, proxima, ultima,
                "Error temporal", "clave-123", ahora, ahora
        );

        assertEquals(idOperacion, operacion.getIdOperacion());
        assertEquals(idReserva, operacion.getIdReserva());
        assertEquals(idPago, operacion.getIdPago());
        assertEquals("CONFIRMAR_PAGO", operacion.getTipoOperacion());
        assertEquals("PENDIENTE", operacion.getEstado());
        assertEquals(2, operacion.getIntentos());
        assertEquals(proxima, operacion.getProximaEjecucion());
        assertEquals(ultima, operacion.getUltimaEjecucion());
        assertEquals("Error temporal", operacion.getUltimoError());
        assertEquals("clave-123", operacion.getClaveIdempotencia());
        assertEquals(ahora, operacion.getFechaCreacion());
        assertEquals(ahora, operacion.getFechaActualizacion());
    }

    @Test
    void settersDebenActualizarTodosLosAtributos() {
        OperacionPendiente operacion = new OperacionPendiente();

        UUID idOperacion = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        UUID idPago = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();

        operacion.setIdOperacion(idOperacion);
        operacion.setIdReserva(idReserva);
        operacion.setIdPago(idPago);
        operacion.setTipoOperacion("REEMBOLSO");
        operacion.setEstado("EN_PROCESO");
        operacion.setIntentos(3);
        operacion.setProximaEjecucion(ahora.plusHours(2));
        operacion.setUltimaEjecucion(ahora);
        operacion.setUltimoError("Error de conexión");
        operacion.setClaveIdempotencia("clave-456");
        operacion.setFechaCreacion(ahora);
        operacion.setFechaActualizacion(ahora.plusMinutes(5));

        assertEquals(idOperacion, operacion.getIdOperacion());
        assertEquals(idReserva, operacion.getIdReserva());
        assertEquals(idPago, operacion.getIdPago());
        assertEquals("REEMBOLSO", operacion.getTipoOperacion());
        assertEquals("EN_PROCESO", operacion.getEstado());
        assertEquals(3, operacion.getIntentos());
        assertEquals(ahora.plusHours(2), operacion.getProximaEjecucion());
        assertEquals(ahora, operacion.getUltimaEjecucion());
        assertEquals("Error de conexión", operacion.getUltimoError());
        assertEquals("clave-456", operacion.getClaveIdempotencia());
        assertEquals(ahora, operacion.getFechaCreacion());
        assertEquals(ahora.plusMinutes(5), operacion.getFechaActualizacion());
    }

    @Test
    void atributosOpcionalesPuedenSerNulos() {
        UUID idOperacion = UUID.randomUUID();
        UUID idReserva = UUID.randomUUID();
        OffsetDateTime ahora = OffsetDateTime.now();

        OperacionPendiente operacion = new OperacionPendiente(
                idOperacion, idReserva, null, "CONFIRMAR_PAGO",
                "PENDIENTE", 0, ahora, null, null,
                "clave-789", ahora, ahora
        );

        assertNull(operacion.getIdPago());
        assertNull(operacion.getUltimaEjecucion());
        assertNull(operacion.getUltimoError());
    }

    @Test
    void debePermitirActualizarElNumeroDeIntentos() {
        OperacionPendiente operacion = new OperacionPendiente();

        operacion.setIntentos(1);
        assertEquals(1, operacion.getIntentos());

        operacion.setIntentos(5);
        assertEquals(5, operacion.getIntentos());
    }

    @Test
    void debeConservarLosOffsetsDeLasFechas() {
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-21T10:30:00-05:00");

        OperacionPendiente operacion = new OperacionPendiente();
        operacion.setProximaEjecucion(fecha);
        operacion.setUltimaEjecucion(fecha);
        operacion.setFechaCreacion(fecha);
        operacion.setFechaActualizacion(fecha);

        assertEquals(fecha, operacion.getProximaEjecucion());
        assertEquals(fecha, operacion.getUltimaEjecucion());
        assertEquals(fecha, operacion.getFechaCreacion());
        assertEquals(fecha, operacion.getFechaActualizacion());
        assertEquals("-05:00", operacion.getFechaCreacion().getOffset().toString());
    }
}
