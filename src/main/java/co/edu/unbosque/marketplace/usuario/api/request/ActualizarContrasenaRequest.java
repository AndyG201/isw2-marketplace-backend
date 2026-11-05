
package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.constraints.NotBlank;

public class ActualizarContrasenaRequest {

    @NotBlank(message = "Debes proporcionar tu contraseña actual.")
    private String contrasenaActual;

    @NotBlank(message = "Debes proporcionar la nueva contraseña.")
    private String nuevaContrasena;

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }
}
