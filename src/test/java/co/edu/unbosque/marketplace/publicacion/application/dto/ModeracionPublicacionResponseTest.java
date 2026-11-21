package co.edu.unbosque.marketplace.publicacion.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ModeracionPublicacionResponseTest {

    @Test
    void debeCrearResponseCorrectamente() {
        UUID idRevision = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        OffsetDateTime fechaOperacion = OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        ModeracionPublicacionResponse response =
                new ModeracionPublicacionResponse(
                        idRevision,
                        idPublicacion,
                        "APROBAR",
                        "PENDIENTE",
                        "PUBLICADA",
                        "La publicación cumple los requisitos",
                        "Publicación aprobada correctamente",
                        fechaOperacion
                );

        assertEquals(idRevision, response.getIdRevision());
        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals("APROBAR", response.getAccion());
        assertEquals("PENDIENTE", response.getEstadoAnterior());
        assertEquals("PUBLICADA", response.getEstadoActual());
        assertEquals("La publicación cumple los requisitos", response.getMotivo());
        assertEquals("Publicación aprobada correctamente", response.getMensaje());
        assertEquals(fechaOperacion, response.getFechaOperacion());
    }

    @Test
    void debeCrearResponseConAccionDeRechazo() {
        UUID idRevision = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        OffsetDateTime fechaOperacion = OffsetDateTime.now();

        ModeracionPublicacionResponse response =
                new ModeracionPublicacionResponse(
                        idRevision,
                        idPublicacion,
                        "RECHAZAR",
                        "PENDIENTE",
                        "RECHAZADA",
                        "Información incompleta",
                        "La publicación fue rechazada",
                        fechaOperacion
                );

        assertEquals("RECHAZAR", response.getAccion());
        assertEquals("PENDIENTE", response.getEstadoAnterior());
        assertEquals("RECHAZADA", response.getEstadoActual());
        assertEquals("Información incompleta", response.getMotivo());
        assertEquals("La publicación fue rechazada", response.getMensaje());
    }

    @Test
    void debeCrearResponseConValoresNulos() {
        ModeracionPublicacionResponse response =
                new ModeracionPublicacionResponse(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertNull(response.getIdRevision());
        assertNull(response.getIdPublicacion());
        assertNull(response.getAccion());
        assertNull(response.getEstadoAnterior());
        assertNull(response.getEstadoActual());
        assertNull(response.getMotivo());
        assertNull(response.getMensaje());
        assertNull(response.getFechaOperacion());
    }

    @Test
    void debeConservarFechaOperacionConZonaHoraria() {
        OffsetDateTime fechaOperacion =
                OffsetDateTime.parse("2026-11-18T15:45:30-05:00");

        ModeracionPublicacionResponse response =
                new ModeracionPublicacionResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "SUSPENDER",
                        "PUBLICADA",
                        "SUSPENDIDA",
                        "Incumplimiento de políticas",
                        "Publicación suspendida",
                        fechaOperacion
                );

        assertNotNull(response.getFechaOperacion());
        assertEquals(fechaOperacion, response.getFechaOperacion());
        assertEquals("-05:00", response.getFechaOperacion().getOffset().toString());
    }
}

