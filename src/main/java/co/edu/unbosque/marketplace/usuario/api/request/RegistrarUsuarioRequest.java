
package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrarUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 50, message = "El apellido no puede superar 50 caracteres.")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio.")
    @Size(max = 254, message = "El correo supera la longitud permitida.")
    private String correo;

    @NotBlank(message = "El tipo de documento es obligatorio.")
    @Size(max = 30, message = "El tipo de documento supera la longitud permitida.")
    private String tipoDocumento;

    @NotBlank(message = "El documento de identidad es obligatorio.")
    private String documentoIdentidad;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 20, message = "El teléfono supera la longitud permitida.")
    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String contrasena;

    @NotBlank(message = "El rol es obligatorio.")
    @Pattern(
        regexp = "(?i)\\s*(HUESPED|ANFITRION)\\s*",
        message = "El rol debe ser HUESPED o ANFITRION."
    )
    private String rol;

    public RegistrarUsuarioRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
