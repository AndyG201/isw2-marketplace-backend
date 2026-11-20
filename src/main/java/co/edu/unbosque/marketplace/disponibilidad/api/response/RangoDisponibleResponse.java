package co.edu.unbosque.marketplace.disponibilidad.api.response;

import java.time.LocalDate;

public record RangoDisponibleResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
