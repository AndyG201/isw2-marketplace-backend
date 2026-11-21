package co.edu.unbosque.marketplace.auditoria.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class EventoAuditoriaResponseTest {

    @Test
    void conservaLosDatosPublicosDelEvento() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        EventoAuditoriaResponse response = new EventoAuditoriaResponse(
                eventId, userId, date, "ACCION", "EXITOSO", "RESERVA", entityId, "{}", "corr-1");

        assertEquals(eventId, response.idEvento());
        assertEquals(userId, response.idUsuario());
        assertEquals(date, response.fechaEvento());
        assertEquals("ACCION", response.accion());
        assertEquals("EXITOSO", response.resultado());
        assertEquals("RESERVA", response.entidadAfectada());
        assertEquals(entityId, response.idEntidadAfectada());
        assertEquals("{}", response.detalleJson());
        assertEquals("corr-1", response.idCorrelacion());
    }
}