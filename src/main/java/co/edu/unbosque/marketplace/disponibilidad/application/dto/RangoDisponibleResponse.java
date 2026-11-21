package co.edu.unbosque.marketplace.disponibilidad.application.dto;

import java.time.LocalDate;

public record RangoDisponibleResponse(
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
