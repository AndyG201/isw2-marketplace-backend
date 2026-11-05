
package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReactivarCuentaRequest {

    @NotBlank(message = "El correo es obligatorio.")
    @Size(max = 254, message = "El correo supera la longitud permitida.")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String contrasena;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
