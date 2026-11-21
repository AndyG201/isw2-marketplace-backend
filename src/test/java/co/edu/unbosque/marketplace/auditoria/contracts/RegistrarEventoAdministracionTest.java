package co.edu.unbosque.marketplace.auditoria.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RegistrarEventoAdministracionTest {

    @Test
    void conservaLosDatosDelEventoAdministrativo() {
        UUID userId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        OffsetDateTime date = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        RegistrarEventoAdministracion request = new RegistrarEventoAdministracion(
                userId, entityId, "USUARIO", date, "RESTRINGIR", "EXITOSO", "{}", "corr-1");

        assertEquals(userId, request.idUsuario());
        assertEquals(entityId, request.idEntidadAfectada());
        assertEquals("USUARIO", request.entidadAfectada());
        assertEquals(date, request.fechaEvento());
        assertEquals("RESTRINGIR", request.accion());
        assertEquals("EXITOSO", request.resultado());
        assertEquals("{}", request.detalle());
        assertEquals("corr-1", request.idCorrelacion());
    }
}