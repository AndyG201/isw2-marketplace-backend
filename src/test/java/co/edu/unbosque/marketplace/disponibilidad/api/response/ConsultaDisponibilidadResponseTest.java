package co.edu.unbosque.marketplace.disponibilidad.api.response;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.disponibilidad.application.dto.ConsultaDisponibilidadResponse;

class ConsultaDisponibilidadResponseTest {

    @Test
    void constructorYAccesores() {
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate fechaInicio = LocalDate.of(2026, 12, 10);
        LocalDate fechaFin = LocalDate.of(2026, 12, 15);
        LocalDate nocheOcupada = LocalDate.of(2026, 12, 12);

        ConsultaDisponibilidadResponse response =
                new ConsultaDisponibilidadResponse(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin,
                        false,
                        true,
                        List.of(nocheOcupada),
                        List.of()
                );

        assertEquals(idAlojamiento, response.idAlojamiento());
        assertEquals(fechaInicio, response.fechaInicio());
        assertEquals(fechaFin, response.fechaFin());
        assertFalse(response.disponible());
        assertTrue(response.cubiertoPorPeriodosDisponibles());
        assertEquals(List.of(nocheOcupada), response.nochesOcupadas());
    }

    @Test
    void constructorPermiteListaDeNochesVacia() {
        ConsultaDisponibilidadResponse response =
                new ConsultaDisponibilidadResponse(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 15),
                        true,
                        true,
                        List.of(),
                        List.of()
                );

        assertTrue(response.nochesOcupadas().isEmpty());
    }

    @Test
    void constructorCopiaLaListaDeNochesOcupadas() {
        List<LocalDate> nochesOcupadas = new ArrayList<>();
        LocalDate noche = LocalDate.of(2026, 12, 12);

        nochesOcupadas.add(noche);

        ConsultaDisponibilidadResponse response =
                new ConsultaDisponibilidadResponse(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 15),
                        false,
                        true,
                        nochesOcupadas,
                        List.of()
                );

        nochesOcupadas.add(LocalDate.of(2026, 12, 13));

        assertEquals(1, response.nochesOcupadas().size());
        assertEquals(noche, response.nochesOcupadas().get(0));
    }

    @Test
    void listaDeNochesOcupadasNoPermiteModificaciones() {
        ConsultaDisponibilidadResponse response =
                new ConsultaDisponibilidadResponse(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 15),
                        false,
                        true,
                        List.of(LocalDate.of(2026, 12, 12)),
                        List.of()
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> response.nochesOcupadas().add(
                        LocalDate.of(2026, 12, 13)
                )
        );
    }

    @Test
    void dosResponsesConLosMismosDatosSonIguales() {
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate fechaInicio = LocalDate.of(2026, 12, 10);
        LocalDate fechaFin = LocalDate.of(2026, 12, 15);
        List<LocalDate> noches = List.of(
                LocalDate.of(2026, 12, 12)
        );

        ConsultaDisponibilidadResponse primero =
                new ConsultaDisponibilidadResponse(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin,
                        false,
                        true,
                        noches,
                        List.of()
                );

        ConsultaDisponibilidadResponse segundo =
                new ConsultaDisponibilidadResponse(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin,
                        false,
                        true,
                        noches,
                        List.of()
                );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void responsesConDiferenteDisponibilidadNoSonIguales() {
        UUID idAlojamiento = UUID.randomUUID();
        LocalDate fechaInicio = LocalDate.of(2026, 12, 10);
        LocalDate fechaFin = LocalDate.of(2026, 12, 15);

        ConsultaDisponibilidadResponse primero =
                new ConsultaDisponibilidadResponse(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin,
                        true,
                        true,
                        List.of(),
                        List.of()
                );

        ConsultaDisponibilidadResponse segundo =
                new ConsultaDisponibilidadResponse(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin,
                        false,
                        true,
                        List.of(),
                        List.of()
                );

        assertNotEquals(primero, segundo);
    }
}