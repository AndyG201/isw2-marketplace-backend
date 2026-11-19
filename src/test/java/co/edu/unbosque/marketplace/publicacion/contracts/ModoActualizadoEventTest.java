package co.edu.unbosque.marketplace.publicacion.contracts;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ModoAceptacionActualizadoEventTest {

    @Test
    void debeCrearEventCorrectamente() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idAnfitrion = UUID.randomUUID();
        OffsetDateTime fechaEvento =
                OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        ModoAceptacionActualizadoEvent event =
                new ModoAceptacionActualizadoEvent(
                        idPublicacion,
                        idAnfitrion,
                        "Apartamento en Bogotá",
                        "MANUAL",
                        "AUTOMATICA",
                        fechaEvento
                );

        assertEquals(idPublicacion, event.idPublicacion());
        assertEquals(idAnfitrion, event.idAnfitrion());
        assertEquals("Apartamento en Bogotá", event.titulo());
        assertEquals("MANUAL", event.modoAnterior());
        assertEquals("AUTOMATICA", event.modoNuevo());
        assertEquals(fechaEvento, event.fechaEvento());
    }

    @Test
    void debeCompararDosEventsIguales() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idAnfitrion = UUID.randomUUID();
        OffsetDateTime fechaEvento =
                OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        ModoAceptacionActualizadoEvent event1 =
                new ModoAceptacionActualizadoEvent(
                        idPublicacion,
                        idAnfitrion,
                        "Apartamento",
                        "MANUAL",
                        "AUTOMATICA",
                        fechaEvento
                );

        ModoAceptacionActualizadoEvent event2 =
                new ModoAceptacionActualizadoEvent(
                        idPublicacion,
                        idAnfitrion,
                        "Apartamento",
                        "MANUAL",
                        "AUTOMATICA",
                        fechaEvento
                );

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void debeDetectarEventsDiferentes() {
        ModoAceptacionActualizadoEvent event1 =
                new ModoAceptacionActualizadoEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Apartamento",
                        "MANUAL",
                        "AUTOMATICA",
                        OffsetDateTime.now()
                );

        ModoAceptacionActualizadoEvent event2 =
                new ModoAceptacionActualizadoEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Casa",
                        "AUTOMATICA",
                        "MANUAL",
                        OffsetDateTime.now()
                );

        assertNotEquals(event1, event2);
    }

    @Test
    void debeConservarZonaHorariaDeFechaEvento() {
        OffsetDateTime fechaEvento =
                OffsetDateTime.parse("2026-11-18T15:45:30-05:00");

        ModoAceptacionActualizadoEvent event =
                new ModoAceptacionActualizadoEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Apartamento",
                        "MANUAL",
                        "AUTOMATICA",
                        fechaEvento
                );

        assertNotNull(event.fechaEvento());
        assertEquals(fechaEvento, event.fechaEvento());
        assertEquals("-05:00", event.fechaEvento().getOffset().toString());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        ModoAceptacionActualizadoEvent event =
                new ModoAceptacionActualizadoEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Apartamento",
                        "MANUAL",
                        "AUTOMATICA",
                        OffsetDateTime.parse("2026-11-18T10:30:00-05:00")
                );

        String resultado = event.toString();

        assertNotNull(resultado);
        assertTrue(resultado.contains("Apartamento"));
        assertTrue(resultado.contains("MANUAL"));
        assertTrue(resultado.contains("AUTOMATICA"));
        assertTrue(resultado.contains("fechaEvento"));
    }
}
