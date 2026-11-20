package co.edu.unbosque.marketplace.auditoria.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class FiltrosAuditoriaTest {

    @Test
    void conservaTodosLosFiltrosDeConsulta() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.parse("2026-11-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-11-20T23:59:59Z");
        FiltrosAuditoria filters = new FiltrosAuditoria(
                userId, from, to, "LOGIN", "EXITOSO", "USUARIO");

        assertEquals(userId, filters.idUsuario());
        assertEquals(from, filters.desde());
        assertEquals(to, filters.hasta());
        assertEquals("LOGIN", filters.accion());
        assertEquals("EXITOSO", filters.resultado());
        assertEquals("USUARIO", filters.entidadAfectada());
    }
}