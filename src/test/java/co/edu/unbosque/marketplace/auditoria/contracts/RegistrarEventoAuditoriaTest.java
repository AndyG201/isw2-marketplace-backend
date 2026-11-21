package co.edu.unbosque.marketplace.auditoria.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RegistrarEventoAuditoriaTest {

    @Test
    void conservaLosDatosDelContratoGenerico() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        RegistrarEventoAuditoria request = new RegistrarEventoAuditoria(
                userId, date, "ACCION", "EXITOSO", "RESERVA", entityId, "{}", "corr-1");

        assertEquals(userId, request.idUsuario());
        assertEquals(date, request.fechaEvento());
        assertEquals("ACCION", request.accion());
        assertEquals("EXITOSO", request.resultado());
        assertEquals("RESERVA", request.entidadAfectada());
        assertEquals(entityId, request.idEntidadAfectada());
        assertEquals("{}", request.detalle());
        assertEquals("corr-1", request.idCorrelacion());
    }
}