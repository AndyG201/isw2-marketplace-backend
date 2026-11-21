package co.edu.unbosque.marketplace.cotizacion.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CrearCotizacionRequest(

        @NotNull(message = "El identificador de la publicación es obligatorio.")
        UUID idPublicacion,

        @NotNull(message = "La fecha de entrada es obligatoria.")
        LocalDate fechaEntrada,

        @NotNull(message = "La fecha de salida es obligatoria.")
        LocalDate fechaSalida,

        @Min(value = 1, message = "Debe indicarse al menos un huésped.")
        int cantidadHuespedes,

        List<@NotNull(message = "El identificador de cada servicio es obligatorio.") UUID> idsAdicionales
) {
}