
package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RestablecerContrasenaRequest {

    @NotBlank(message = "Debes proporcionar un correo.")
    @Size(max = 254, message = "El correo supera la longitud permitida.")
    private String correo;

    @NotBlank(message = "Debes proporcionar el código.")
    @Pattern(
        regexp = "\\d{6}",
        message = "El código debe contener exactamente 6 dígitos."
    )
    private String codigo;

    @NotBlank(message = "Debes proporcionar la nueva contraseña.")
    private String nuevaContrasena;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }
}
