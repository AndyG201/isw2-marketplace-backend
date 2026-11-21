package co.edu.unbosque.marketplace.auditoria.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class EventoAuditoriaTest {

    @Test
    void conservaLosComponentesDelEvento() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        UUID entityId = UUID.randomUUID();
        EventoAuditoria event = new EventoAuditoria(eventId, userId, date,
                "ACCION", "EXITOSO", "USUARIO", entityId, "{}", "corr-1");

        assertEquals(eventId, event.idEvento());
        assertEquals(userId, event.idUsuario());
        assertEquals(date, event.fechaEvento());
        assertEquals("ACCION", event.accion());
        assertEquals("EXITOSO", event.resultado());
        assertEquals("USUARIO", event.entidadAfectada());
        assertEquals(entityId, event.idEntidadAfectada());
        assertEquals("{}", event.detalleJson());
        assertEquals("corr-1", event.idCorrelacion());
    }
}