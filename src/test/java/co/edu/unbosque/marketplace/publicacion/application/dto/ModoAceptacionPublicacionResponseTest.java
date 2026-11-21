package co.edu.unbosque.marketplace.publicacion.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ModoAceptacionPublicacionResponseTest {

    @Test
    void debeCrearResponseCorrectamente() {
        UUID idPublicacion = UUID.randomUUID();
        OffsetDateTime fechaActualizacion =
                OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        ModoAceptacionPublicacionResponse response =
                new ModoAceptacionPublicacionResponse(
                        idPublicacion,
                        "AUTOMATICA",
                        fechaActualizacion
                );

        assertEquals(idPublicacion, response.idPublicacion());
        assertEquals("AUTOMATICA", response.modoAceptacion());
        assertEquals(fechaActualizacion, response.fechaActualizacion());
    }

    @Test
    void debeCompararDosResponsesIguales() {
        UUID idPublicacion = UUID.randomUUID();
        OffsetDateTime fechaActualizacion =
                OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        ModoAceptacionPublicacionResponse response1 =
                new ModoAceptacionPublicacionResponse(
                        idPublicacion, "MANUAL", fechaActualizacion
                );

        ModoAceptacionPublicacionResponse response2 =
                new ModoAceptacionPublicacionResponse(
                        idPublicacion, "MANUAL", fechaActualizacion
                );

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void debeDetectarResponsesDiferentes() {
        ModoAceptacionPublicacionResponse response1 =
                new ModoAceptacionPublicacionResponse(
                        UUID.randomUUID(),
                        "AUTOMATICA",
                        OffsetDateTime.now()
                );

        ModoAceptacionPublicacionResponse response2 =
                new ModoAceptacionPublicacionResponse(
                        UUID.randomUUID(),
                        "MANUAL",
                        OffsetDateTime.now()
                );

        assertNotEquals(response1, response2);
    }

    @Test
    void debeConservarZonaHorariaDeFechaActualizacion() {
        OffsetDateTime fechaActualizacion =
                OffsetDateTime.parse("2026-11-18T15:45:30-05:00");

        ModoAceptacionPublicacionResponse response =
                new ModoAceptacionPublicacionResponse(
                        UUID.randomUUID(),
                        "MANUAL",
                        fechaActualizacion
                );

        assertNotNull(response.fechaActualizacion());
        assertEquals(fechaActualizacion, response.fechaActualizacion());
        assertEquals("-05:00",
                response.fechaActualizacion().getOffset().toString());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        ModoAceptacionPublicacionResponse response =
                new ModoAceptacionPublicacionResponse(
                        UUID.randomUUID(),
                        "AUTOMATICA",
                        OffsetDateTime.parse("2026-11-18T10:30:00-05:00")
                );

        String resultado = response.toString();

        assertNotNull(resultado);
        assertTrue(resultado.contains("AUTOMATICA"));
        assertTrue(resultado.contains("fechaActualizacion"));
    }
}
