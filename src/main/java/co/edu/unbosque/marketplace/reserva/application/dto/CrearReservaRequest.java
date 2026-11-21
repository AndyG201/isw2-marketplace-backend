package co.edu.unbosque.marketplace.reserva.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CrearReservaRequest(
        @NotNull(message = "La cotización es obligatoria.")
        UUID idCotizacion) {
}