package co.edu.unbosque.marketplace.auditoria.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RegistrarEventoResenaTest {

    @Test
    void conservaLosDatosDelEventoDeResena() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        RegistrarEventoResena request = new RegistrarEventoResena(
                userId, reviewId, date, "OCULTAR", "EXITOSO", "{}");

        assertEquals(userId, request.idUsuario());
        assertEquals(reviewId, request.idResena());
        assertEquals(date, request.fechaEvento());
        assertEquals("OCULTAR", request.accion());
        assertEquals("EXITOSO", request.resultado());
        assertEquals("{}", request.detalle());
    }
}