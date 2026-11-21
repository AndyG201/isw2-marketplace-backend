package co.edu.unbosque.marketplace.usuario.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "correo_pendiente")
public class CorreoPendiente {
    @Id
    @Column(name = "id_correo", nullable = false)
    private UUID idCorreo;
    @Column(name = "destinatario", nullable = false, length = 320)
    private String destinatario;
    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;
    @Column(name = "contenido_cifrado", nullable = false)
    private byte[] contenidoCifrado;
    @Column(name = "nonce", nullable = false)
    private byte[] nonce;
    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 100)
    private String claveIdempotencia;
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;
    @Column(name = "intentos", nullable = false)
    private int intentos;
    @Column(name = "proxima_ejecucion", nullable = false)
    private OffsetDateTime proximaEjecucion;
    @Column(name = "lease_hasta")
    private OffsetDateTime leaseHasta;
    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;
    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;
    @Column(name = "ultimo_error", columnDefinition = "TEXT")
    private String ultimoError;

    public UUID getIdCorreo() { return idCorreo; }
    public void setIdCorreo(UUID v) { idCorreo = v; }
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String v) { destinatario = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String v) { tipo = v; }
    public byte[] getContenidoCifrado() { return contenidoCifrado; }
    public void setContenidoCifrado(byte[] v) { contenidoCifrado = v; }
    public byte[] getNonce() { return nonce; }
    public void setNonce(byte[] v) { nonce = v; }
    public String getClaveIdempotencia() { return claveIdempotencia; }
    public void setClaveIdempotencia(String v) { claveIdempotencia = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { estado = v; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int v) { intentos = v; }
    public OffsetDateTime getProximaEjecucion() { return proximaEjecucion; }
    public void setProximaEjecucion(OffsetDateTime v) { proximaEjecucion = v; }
    public OffsetDateTime getLeaseHasta() { return leaseHasta; }
    public void setLeaseHasta(OffsetDateTime v) { leaseHasta = v; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime v) { fechaCreacion = v; }
    public OffsetDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(OffsetDateTime v) { fechaEnvio = v; }
    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String v) { ultimoError = v; }
}
