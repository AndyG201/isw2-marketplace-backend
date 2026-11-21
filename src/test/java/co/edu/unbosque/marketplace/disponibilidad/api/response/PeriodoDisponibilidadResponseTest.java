package co.edu.unbosque.marketplace.disponibilidad.api.response;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.disponibilidad.application.dto.PeriodoDisponibilidadResponse;

class PeriodoDisponibilidadResponseTest {

    @Test
    void debeCrearResponseConTodosLosDatos() {
        UUID idPeriodo = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate fechaInicio = LocalDate.of(2026, 12, 10);
        LocalDate fechaFin = LocalDate.of(2026, 12, 15);
        OffsetDateTime fechaCreacion = OffsetDateTime.now();

        PeriodoDisponibilidadResponse response =
                new PeriodoDisponibilidadResponse(
                        idPeriodo,
                        idAlojamiento,
                        fechaInicio,
                        fechaFin,
                        true,
                        fechaCreacion
                );

        assertEquals(idPeriodo, response.idPeriodo());
        assertEquals(idAlojamiento, response.idAlojamiento());
        assertEquals(fechaInicio, response.fechaInicio());
        assertEquals(fechaFin, response.fechaFin());
        assertTrue(response.habilitado());
        assertEquals(fechaCreacion, response.fechaCreacion());
    }

    @Test
    void debePermitirValoresNulos() {
        PeriodoDisponibilidadResponse response =
                new PeriodoDisponibilidadResponse(
                        null,
                        null,
                        null,
                        null,
                        false,
                        null
                );

        assertNull(response.idPeriodo());
        assertNull(response.idAlojamiento());
        assertNull(response.fechaInicio());
        assertNull(response.fechaFin());
        assertFalse(response.habilitado());
        assertNull(response.fechaCreacion());
    }

    @Test
    void debeCompararDosResponsesConLosMismosDatos() {
        UUID idPeriodo = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate fechaInicio = LocalDate.of(2026, 12, 10);
        LocalDate fechaFin = LocalDate.of(2026, 12, 15);
        OffsetDateTime fechaCreacion = OffsetDateTime.now();

        PeriodoDisponibilidadResponse primero =
                new PeriodoDisponibilidadResponse(
                        idPeriodo, idAlojamiento,
                        fechaInicio, fechaFin, true, fechaCreacion
                );

        PeriodoDisponibilidadResponse segundo =
                new PeriodoDisponibilidadResponse(
                        idPeriodo, idAlojamiento,
                        fechaInicio, fechaFin, true, fechaCreacion
                );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void debeDetectarResponsesConDatosDiferentes() {
        PeriodoDisponibilidadResponse primero =
                new PeriodoDisponibilidadResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 15),
                        true,
                        OffsetDateTime.now()
                );

        PeriodoDisponibilidadResponse segundo =
                new PeriodoDisponibilidadResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 15),
                        false,
                        OffsetDateTime.now()
                );

        assertNotEquals(primero, segundo);
    }

    @Test
    void debeGenerarToStringConLosDatos() {
        UUID idPeriodo = UUID.randomUUID();
        LocalDate fechaInicio = LocalDate.of(2026, 12, 10);

        PeriodoDisponibilidadResponse response =
                new PeriodoDisponibilidadResponse(
                        idPeriodo,
                        UUID.randomUUID(),
                        fechaInicio,
                        LocalDate.of(2026, 12, 15),
                        true,
                        OffsetDateTime.now()
                );

        assertTrue(response.toString().contains(idPeriodo.toString()));
        assertTrue(response.toString().contains(fechaInicio.toString()));
    }
}
