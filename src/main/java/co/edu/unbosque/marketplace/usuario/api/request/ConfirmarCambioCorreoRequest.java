
package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ConfirmarCambioCorreoRequest {

    @NotBlank(message = "Debes proporcionar el código.")
    @Pattern(
        regexp = "\\d{6}",
        message = "El código debe contener exactamente 6 dígitos."
    )
    private String codigo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
