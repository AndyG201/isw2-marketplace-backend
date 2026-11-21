package co.edu.unbosque.marketplace.auditoria.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RegistrarEventoReservaTest {

    @Test
    void conservaLosDatosDelEventoDeReserva() {
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        RegistrarEventoReserva request = new RegistrarEventoReserva(
                userId, reservationId, date, "CANCELAR", "EXITOSO", "{}");

        assertEquals(userId, request.idUsuario());
        assertEquals(reservationId, request.idReserva());
        assertEquals(date, request.fechaEvento());
        assertEquals("CANCELAR", request.accion());
        assertEquals("EXITOSO", request.resultado());
        assertEquals("{}", request.detalle());
    }
}