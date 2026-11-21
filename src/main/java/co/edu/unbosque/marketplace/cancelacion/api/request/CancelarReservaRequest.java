package co.edu.unbosque.marketplace.cancelacion.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarReservaRequest(
        @NotBlank(message = "El motivo de cancelación es obligatorio.")
        @Size(max = 1000, message = "El motivo no puede superar 1000 caracteres.")
        String motivo) {
}
