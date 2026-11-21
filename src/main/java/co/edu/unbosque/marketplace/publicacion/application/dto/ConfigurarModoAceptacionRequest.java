package co.edu.unbosque.marketplace.publicacion.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ConfigurarModoAceptacionRequest {

    @NotBlank(message = "El modo de aceptación es obligatorio.")
    @Pattern(regexp = "INMEDIATA|APROBACION|\\s*",
            message = "El modo debe ser INMEDIATA o APROBACION."
    )
    private String modoAceptacion;

    public String getModoAceptacion() {
        return modoAceptacion;
    }

    public void setModoAceptacion(String modoAceptacion) {
        this.modoAceptacion = modoAceptacion;
    }
}
