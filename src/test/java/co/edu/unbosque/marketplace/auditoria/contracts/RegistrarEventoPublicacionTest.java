package co.edu.unbosque.marketplace.auditoria.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RegistrarEventoPublicacionTest {

    @Test
    void conservaLosDatosDelEventoDePublicacion() {
        UUID userId = UUID.randomUUID();
        UUID publicationId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        RegistrarEventoPublicacion request = new RegistrarEventoPublicacion(
                userId, publicationId, date, "APROBAR", "EXITOSO", "{}");

        assertEquals(userId, request.idUsuario());
        assertEquals(publicationId, request.idPublicacion());
        assertEquals(date, request.fechaEvento());
        assertEquals("APROBAR", request.accion());
        assertEquals("EXITOSO", request.resultado());
        assertEquals("{}", request.detalle());
    }
}