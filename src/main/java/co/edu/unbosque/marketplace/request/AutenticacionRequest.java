
package co.edu.unbosque.marketplace.request;

public class AutenticacionRequest {

    private String correo;
    private String contrasena;

    public AutenticacionRequest() {
    }

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
