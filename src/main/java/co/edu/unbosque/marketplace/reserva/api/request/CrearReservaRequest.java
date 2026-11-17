package co.edu.unbosque.marketplace.reserva.api.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CrearReservaRequest(
        @NotNull(message = "La cotización es obligatoria.")
        UUID idCotizacion) {
}