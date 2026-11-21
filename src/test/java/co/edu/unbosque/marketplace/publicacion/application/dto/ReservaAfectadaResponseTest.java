package co.edu.unbosque.marketplace.publicacion.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReservaAfectadaResponseTest {

    @Test
    void debeCrearReservaAfectadaResponseCorrectamente() {
        UUID idReserva = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate fechaEntrada = LocalDate.of(2026, 12, 10);
        LocalDate fechaSalida = LocalDate.of(2026, 12, 15);

        ReservaAfectadaResponse response = new ReservaAfectadaResponse(
                idReserva,
                idPublicacion,
                "Apartamento en Bogotá",
                idAlojamiento,
                "Juan Pérez",
                fechaEntrada,
                fechaSalida,
                "CONFIRMADA",
                "Bloqueo administrativo"
        );

        assertEquals(idReserva, response.idReserva());
        assertEquals(idPublicacion, response.idPublicacion());
        assertEquals("Apartamento en Bogotá", response.tituloPublicacion());
        assertEquals(idAlojamiento, response.idAlojamiento());
        assertEquals("Juan Pérez", response.nombreHuesped());
        assertEquals(fechaEntrada, response.fechaEntrada());
        assertEquals(fechaSalida, response.fechaSalida());
        assertEquals("CONFIRMADA", response.estadoReserva());
        assertEquals("Bloqueo administrativo", response.motivoBloqueo());
    }

    @Test
    void debeCompararDosObjetosIguales() {
        UUID idReserva = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();

        ReservaAfectadaResponse response1 = new ReservaAfectadaResponse(
                idReserva, idPublicacion, "Apartamento",
                idAlojamiento, "Juan Pérez",
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                "CONFIRMADA", "Bloqueo administrativo"
        );

        ReservaAfectadaResponse response2 = new ReservaAfectadaResponse(
                idReserva, idPublicacion, "Apartamento",
                idAlojamiento, "Juan Pérez",
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                "CONFIRMADA", "Bloqueo administrativo"
        );

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void debeDetectarObjetosDiferentes() {
        ReservaAfectadaResponse response1 = new ReservaAfectadaResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Apartamento",
                UUID.randomUUID(), "Juan Pérez",
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                "CONFIRMADA", "Bloqueo administrativo"
        );

        ReservaAfectadaResponse response2 = new ReservaAfectadaResponse(
                UUID.randomUUID(), UUID.randomUUID(), "Casa",
                UUID.randomUUID(), "María López",
                LocalDate.of(2026, 12, 20),
                LocalDate.of(2026, 12, 25),
                "CANCELADA", "Mantenimiento"
        );

        assertNotEquals(response1, response2);
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        ReservaAfectadaResponse response = new ReservaAfectadaResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Apartamento",
                UUID.randomUUID(),
                "Juan Pérez",
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                "CONFIRMADA",
                "Bloqueo administrativo"
        );

        String resultado = response.toString();

        assertNotNull(resultado);
        assertTrue(resultado.contains("Apartamento"));
        assertTrue(resultado.contains("Juan Pérez"));
        assertTrue(resultado.contains("CONFIRMADA"));
        assertTrue(resultado.contains("Bloqueo administrativo"));
    }
}
