package co.edu.unbosque.marketplace.publicacion.contracts;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PublicacionDesbloqueadaEventTest {

    @Test
    void debeCrearEventCorrectamente() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idAnfitrion = UUID.randomUUID();
        OffsetDateTime fechaEvento =
                OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        PublicacionDesbloqueadaEvent event =
                new PublicacionDesbloqueadaEvent(
                        idPublicacion,
                        idAnfitrion,
                        "Apartamento en Bogotá",
                        "Se solucionaron los inconvenientes",
                        fechaEvento
                );

        assertEquals(idPublicacion, event.idPublicacion());
        assertEquals(idAnfitrion, event.idAnfitrion());
        assertEquals("Apartamento en Bogotá", event.titulo());
        assertEquals("Se solucionaron los inconvenientes", event.motivo());
        assertEquals(fechaEvento, event.fechaEvento());
    }

    @Test
    void debeCompararDosEventsIguales() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idAnfitrion = UUID.randomUUID();
        OffsetDateTime fechaEvento =
                OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        PublicacionDesbloqueadaEvent event1 =
                new PublicacionDesbloqueadaEvent(
                        idPublicacion,
                        idAnfitrion,
                        "Apartamento",
                        "Problema solucionado",
                        fechaEvento
                );

        PublicacionDesbloqueadaEvent event2 =
                new PublicacionDesbloqueadaEvent(
                        idPublicacion,
                        idAnfitrion,
                        "Apartamento",
                        "Problema solucionado",
                        fechaEvento
                );

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void debeDetectarEventsDiferentes() {
        PublicacionDesbloqueadaEvent event1 =
                new PublicacionDesbloqueadaEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Apartamento",
                        "Problema solucionado",
                        OffsetDateTime.now()
                );

        PublicacionDesbloqueadaEvent event2 =
                new PublicacionDesbloqueadaEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Casa",
                        "Documentación actualizada",
                        OffsetDateTime.now()
                );

        assertNotEquals(event1, event2);
    }

    @Test
    void debeConservarZonaHorariaDeFechaEvento() {
        OffsetDateTime fechaEvento =
                OffsetDateTime.parse("2026-11-18T15:45:30-05:00");

        PublicacionDesbloqueadaEvent event =
                new PublicacionDesbloqueadaEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Apartamento",
                        "Bloqueo levantado",
                        fechaEvento
                );

        assertNotNull(event.fechaEvento());
        assertEquals(fechaEvento, event.fechaEvento());
        assertEquals("-05:00", event.fechaEvento().getOffset().toString());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        PublicacionDesbloqueadaEvent event =
                new PublicacionDesbloqueadaEvent(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Apartamento",
                        "Bloqueo levantado",
                        OffsetDateTime.parse("2026-11-18T10:30:00-05:00")
                );

        String resultado = event.toString();

        assertNotNull(resultado);
        assertTrue(resultado.contains("Apartamento"));
        assertTrue(resultado.contains("Bloqueo levantado"));
        assertTrue(resultado.contains("fechaEvento"));
    }
}
