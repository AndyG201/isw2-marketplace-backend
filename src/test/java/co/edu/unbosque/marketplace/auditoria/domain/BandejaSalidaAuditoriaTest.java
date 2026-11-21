package co.edu.unbosque.marketplace.auditoria.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class BandejaSalidaAuditoriaTest {

    @Test
    void constructorVacioPermiteHidratarYLeerTodosLosCampos() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:15:30Z");
        BandejaSalidaAuditoria event = new BandejaSalidaAuditoria();

        event.setIdEvento(eventId);
        event.setIdUsuario(userId);
        event.setFechaEvento(date);
        event.setAccion("RESERVA_CANCELADA");
        event.setResultado("EXITOSO");
        event.setEntidadAfectada("RESERVA");
        event.setIdEntidadAfectada(entityId);
        event.setDetalle("{}");
        event.setIdCorrelacion("corr-1");
        event.setEstadoEnvio("PENDIENTE");
        event.setIntentosEnvio(0);
        event.setProximaEjecucion(date);
        event.setFechaEnvio(null);
        event.setUltimoError(null);

        assertEquals(eventId, event.getIdEvento());
        assertEquals(userId, event.getIdUsuario());
        assertEquals(date, event.getFechaEvento());
        assertEquals("RESERVA_CANCELADA", event.getAccion());
        assertEquals("EXITOSO", event.getResultado());
        assertEquals("RESERVA", event.getEntidadAfectada());
        assertEquals(entityId, event.getIdEntidadAfectada());
        assertEquals("{}", event.getDetalle());
        assertEquals("corr-1", event.getIdCorrelacion());
        assertEquals("PENDIENTE", event.getEstadoEnvio());
        assertEquals(0, event.getIntentosEnvio());
        assertEquals(date, event.getProximaEjecucion());
        assertNull(event.getFechaEnvio());
        assertNull(event.getUltimoError());
    }
}