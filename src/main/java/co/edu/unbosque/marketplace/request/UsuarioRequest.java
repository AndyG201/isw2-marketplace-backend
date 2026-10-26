package co.edu.unbosque.marketplace.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import co.edu.unbosque.marketplace.model.emun.EstadoUsuario;

public class UsuarioRequest{

    private UUID idUsuario;
    private UUID idRol;

    private String nombre;
    private String apellido;
    private String correo;

    private String tipoDocumento;
    private byte[] documentoCifrado;
    private byte[] documentoNonce;
    private byte[] documentoHash;

    private String telefono;
    private String contrasenaHash;

    private boolean correoVerificado;
    private EstadoUsuario estado;
    private boolean restringido;

    private int intentosFallidos;
    private String motivoRestriccion;

    private OffsetDateTime fechaRegistro;
    private OffsetDateTime fechaActualizacion;

    public UsuarioRequest() {
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public UUID getIdRol() {
        return idRol;
    }

    public void setIdRol(UUID idRol) {
        this.idRol = idRol;
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

    public byte[] getDocumentoCifrado() {
        return documentoCifrado;
    }

    public void setDocumentoCifrado(byte[] documentoCifrado) {
        this.documentoCifrado = documentoCifrado;
    }

    public byte[] getDocumentoNonce() {
        return documentoNonce;
    }

    public void setDocumentoNonce(byte[] documentoNonce) {
        this.documentoNonce = documentoNonce;
    }

    public byte[] getDocumentoHash() {
        return documentoHash;
    }

    public void setDocumentoHash(byte[] documentoHash) {
        this.documentoHash = documentoHash;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isCorreoVerificado() {
        return correoVerificado;
    }

    public void setCorreoVerificado(boolean correoVerificado) {
        this.correoVerificado = correoVerificado;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public boolean isRestringido() {
        return restringido;
    }

    public void setRestringido(boolean restringido) {
        this.restringido = restringido;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public String getMotivoRestriccion() {
        return motivoRestriccion;
    }

    public void setMotivoRestriccion(String motivoRestriccion) {
        this.motivoRestriccion = motivoRestriccion;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            OffsetDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
}

