package co.edu.unbosque.marketplace.reporte.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SolicitarExportacionRequest(
        @NotBlank(message = "El formato es obligatorio.")
        @Pattern(
                regexp = "(?i)PDF|EXCEL",
                message = "El formato debe ser PDF o EXCEL.")
        String formato) {
}