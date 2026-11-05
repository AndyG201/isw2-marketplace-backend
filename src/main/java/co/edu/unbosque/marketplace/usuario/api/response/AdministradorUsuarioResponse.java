
package co.edu.unbosque.marketplace.usuario.api.response;

import java.util.UUID;

public class AdministradorUsuarioResponse {

    private UUID idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;
    private String estado;
    private boolean restringido;
    private String motivoRestriccion;

    public AdministradorUsuarioResponse(
            UUID idUsuario,
            String nombre,
            String apellido,
            String correo,
            String rol,
            String estado,
            boolean restringido,
            String motivoRestriccion) {

        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
        this.restringido = restringido;
        this.motivoRestriccion = motivoRestriccion;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }

    public String getEstado() {
        return estado;
    }

    public boolean isRestringido() {
        return restringido;
    }

    public String getMotivoRestriccion() {
        return motivoRestriccion;
    }
}
