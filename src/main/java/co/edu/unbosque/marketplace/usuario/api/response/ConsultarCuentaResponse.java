
package co.edu.unbosque.marketplace.usuario.api.response;

public class ConsultarCuentaResponse {

    private String nombre;
    private String apellido;
    private String correo;
    private String tipoDocumento;
    private String documentoIdentidad;
    private String rol;

    public ConsultarCuentaResponse(
            String nombre,
            String apellido,
            String correo,
            String tipoDocumento,
            String documentoIdentidad,
            String rol) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.tipoDocumento = tipoDocumento;
        this.documentoIdentidad = documentoIdentidad;
        this.rol = rol;
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public String getRol() {
        return rol;
    }
}
