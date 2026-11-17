package co.edu.unbosque.marketplace.resena.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearResenaRequest(
        @Min(value = 1, message = "La calificación mínima es 1.")
        @Max(value = 5, message = "La calificación máxima es 5.")
        int calificacion,
        @NotBlank(message = "El comentario de la reseña es obligatorio.")
        @Size(max = 5000, message = "El comentario no puede superar 5000 caracteres.")
        String comentario) {
}
