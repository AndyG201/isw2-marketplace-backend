package co.edu.unbosque.marketplace.pago.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reembolso_pago")
public class ReembolsoPago {

    @Id
    @Column(name = "id_reembolso", nullable = false)
    private UUID idReembolso;

    @Column(name = "id_pago", nullable = false, unique = true)
    private UUID idPago;

    @Column(name = "id_reserva", nullable = false)
    private UUID idReserva;

    @Column(name = "payment_intent_id", nullable = false, length = 255)
    private String paymentIntentId;

    @Column(name = "stripe_refund_id", length = 255)
    private String stripeRefundId;

    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 100)
    private String claveIdempotencia;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "proxima_ejecucion", nullable = false)
    private OffsetDateTime proximaEjecucion;

    @Column(name = "ultima_ejecucion")
    private OffsetDateTime ultimaEjecucion;

    @Column(name = "ultimo_error")
    private String ultimoError;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    protected ReembolsoPago() { }

    public ReembolsoPago(UUID idReembolso, UUID idPago, UUID idReserva, String paymentIntentId,
            String claveIdempotencia, OffsetDateTime ahora) {
        this.idReembolso = idReembolso;
        this.idPago = idPago;
        this.idReserva = idReserva;
        this.paymentIntentId = paymentIntentId;
        this.claveIdempotencia = claveIdempotencia;
        this.estado = "PENDIENTE";
        this.intentos = 0;
        this.proximaEjecucion = ahora;
        this.fechaCreacion = ahora;
        this.fechaActualizacion = ahora;
    }

    public UUID getIdReembolso() { return idReembolso; }
    public UUID getIdPago() { return idPago; }
    public UUID getIdReserva() { return idReserva; }
    public String getPaymentIntentId() { return paymentIntentId; }
    public String getStripeRefundId() { return stripeRefundId; }
    public void setStripeRefundId(String value) { this.stripeRefundId = value; }
    public String getClaveIdempotencia() { return claveIdempotencia; }
    public String getEstado() { return estado; }
    public void setEstado(String value) { this.estado = value; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int value) { this.intentos = value; }
    public OffsetDateTime getProximaEjecucion() { return proximaEjecucion; }
    public void setProximaEjecucion(OffsetDateTime value) { this.proximaEjecucion = value; }
    public OffsetDateTime getUltimaEjecucion() { return ultimaEjecucion; }
    public void setUltimaEjecucion(OffsetDateTime value) { this.ultimaEjecucion = value; }
    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String value) { this.ultimoError = value; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(OffsetDateTime value) { this.fechaActualizacion = value; }
}
