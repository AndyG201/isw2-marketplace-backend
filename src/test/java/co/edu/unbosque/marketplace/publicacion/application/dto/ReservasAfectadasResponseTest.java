package co.edu.unbosque.marketplace.publicacion.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReservasAfectadasResponseTest {

    private ReservaAfectadaResponse reserva() {
        return new ReservaAfectadaResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Cabaña en el lago",
                UUID.randomUUID(),
                "Ana Pérez",
                LocalDate.parse("2026-12-01"),
                LocalDate.parse("2026-12-04"),
                "CONFIRMADA",
                "Mantenimiento");
    }

    @Test
    void conservaElMensajeYLasReservas() {
        ReservaAfectadaResponse reserva = reserva();

        ReservasAfectadasResponse respuesta = new ReservasAfectadasResponse("Hay reservas afectadas.", List.of(reserva));

        assertEquals("Hay reservas afectadas.", respuesta.mensaje());
        assertEquals(List.of(reserva), respuesta.reservas());
    }

    @Test
    void sinListaDeReservasQuedaVacia() {
        ReservasAfectadasResponse respuesta = new ReservasAfectadasResponse("Sin reservas.", null);

        assertTrue(respuesta.reservas().isEmpty());
    }

    @Test
    void guardaUnaCopiaQueNoCambiaSiSeModificaLaListaOriginal() {
        List<ReservaAfectadaResponse> original = new ArrayList<>(List.of(reserva()));
        ReservasAfectadasResponse respuesta = new ReservasAfectadasResponse("Hay reservas afectadas.", original);

        original.add(reserva());

        assertEquals(1, respuesta.reservas().size());
    }

    @Test
    void laListaDeReservasNoSePuedeModificarDesdeFuera() {
        ReservasAfectadasResponse respuesta = new ReservasAfectadasResponse("Hay reservas afectadas.",
                List.of(reserva()));
        List<ReservaAfectadaResponse> reservas = respuesta.reservas();
        ReservaAfectadaResponse otra = reserva();

        assertThrows(UnsupportedOperationException.class, () -> reservas.add(otra));
    }
}
