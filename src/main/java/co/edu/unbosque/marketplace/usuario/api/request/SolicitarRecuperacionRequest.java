
package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SolicitarRecuperacionRequest {

    @NotBlank(message = "Debes proporcionar un correo.")
    @Size(max = 254, message = "El correo supera la longitud permitida.")
    private String correo;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
