package co.edu.unbosque.marketplace.resena.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OcultarResenaRequest(
        @NotBlank(message = "El motivo del ocultamiento es obligatorio.")
        @Size(max = 2000, message = "El motivo no puede superar 2000 caracteres.")
        String motivo) {
}
