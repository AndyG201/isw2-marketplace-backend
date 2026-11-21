package co.edu.unbosque.marketplace.usuario.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ModificarDocumentoRequest(
        @NotBlank(message = "El tipo de documento es obligatorio.")
        @Pattern(
                regexp = "(?i)\\s*(CC|CE|TI|PASAPORTE)\\s*",
                message = "El tipo de documento debe ser CC, CE, TI o PASAPORTE.")
        String tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio.")
        @Pattern(
                regexp = "\\s*[A-Za-z0-9]{5,20}\\s*",
                message = "El número de documento debe tener entre 5 y 20 letras o números.")
        String documentoIdentidad) {
}
