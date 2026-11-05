
package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestringirUsuarioRequest {

    @NotBlank(message = "El motivo de restricción es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar 255 caracteres.")
    private String motivo;

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
