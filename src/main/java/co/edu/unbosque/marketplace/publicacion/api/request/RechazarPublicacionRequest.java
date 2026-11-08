
package co.edu.unbosque.marketplace.publicacion.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RechazarPublicacionRequest {

    @NotBlank(message = "Debes proporcionar el motivo del rechazo.")
    @Size(max = 2000, message = "El motivo no puede superar los 2000 caracteres.")
    private String motivo;

    public RechazarPublicacionRequest() {
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
