package co.edu.unbosque.marketplace.disponibilidad.api.response;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.disponibilidad.application.dto.CalendarioDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.application.dto.PeriodoDisponibilidadResponse;

class CalendarioDisponibilidadResponseTest {

    @Test
    void constructorYAccesores() {
        UUID idAlojamiento = UUID.randomUUID();

        PeriodoDisponibilidadResponse periodo =
                new PeriodoDisponibilidadResponse(idAlojamiento, idAlojamiento, null, null, false, null);

        LocalDate nocheOcupada = LocalDate.of(2026, 12, 12);

        CalendarioDisponibilidadResponse response =
                new CalendarioDisponibilidadResponse(
                        idAlojamiento,
                        List.of(periodo),
                        List.of(nocheOcupada)
                );

        assertEquals(idAlojamiento, response.idAlojamiento());
        assertEquals(List.of(periodo), response.periodos());
        assertEquals(List.of(nocheOcupada), response.nochesOcupadas());
    }

    @Test
    void constructorPermiteListasVacias() {
        CalendarioDisponibilidadResponse response =
                new CalendarioDisponibilidadResponse(
                        UUID.randomUUID(),
                        List.of(),
                        List.of()
                );

        assertTrue(response.periodos().isEmpty());
        assertTrue(response.nochesOcupadas().isEmpty());
    }

    @Test
    void constructorCopiaLasListasRecibidas() {
        List<PeriodoDisponibilidadResponse> periodos =
                new ArrayList<>();

        List<LocalDate> nochesOcupadas =
                new ArrayList<>();

        LocalDate noche = LocalDate.of(2026, 12, 12);
        nochesOcupadas.add(noche);

        CalendarioDisponibilidadResponse response =
                new CalendarioDisponibilidadResponse(
                        UUID.randomUUID(),
                        periodos,
                        nochesOcupadas
                );

        periodos.add(new PeriodoDisponibilidadResponse(null, null, noche, noche, false, null));
        nochesOcupadas.add(LocalDate.of(2026, 12, 13));

        assertTrue(response.periodos().isEmpty());
        assertEquals(1, response.nochesOcupadas().size());
        assertEquals(noche, response.nochesOcupadas().get(0));
    }

    @Test
    void listasDelResponseNoPermitenModificaciones() {
        CalendarioDisponibilidadResponse response =
                new CalendarioDisponibilidadResponse(
                        UUID.randomUUID(),
                        List.of(),
                        List.of(LocalDate.of(2026, 12, 12))
                );

        assertThrows(UnsupportedOperationException.class,
                () -> response.periodos().add(
                        new PeriodoDisponibilidadResponse(null, null, null, null, false, null)
                ));

        assertThrows(UnsupportedOperationException.class,
                () -> response.nochesOcupadas().add(
                        LocalDate.of(2026, 12, 13)
                ));
    }

    @Test
    void dosResponsesConLosMismosDatosSonIguales() {
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate noche = LocalDate.of(2026, 12, 12);

        CalendarioDisponibilidadResponse primero =
                new CalendarioDisponibilidadResponse(
                        idAlojamiento,
                        List.of(),
                        List.of(noche)
                );

        CalendarioDisponibilidadResponse segundo =
                new CalendarioDisponibilidadResponse(
                        idAlojamiento,
                        List.of(),
                        List.of(noche)
                );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }
}
