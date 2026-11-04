package co.edu.unbosque.marketplace.pago.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @Column(name = "id_pago", nullable = false)
    private UUID idPago;

    @Column(name = "id_reserva", nullable = false)
    private UUID idReserva;

    @Column(name = "referencia_stripe", length = 255)
    private String referenciaStripe;

    @Column(name = "clave_idempotencia", length = 100, nullable = false)
    private String claveIdempotencia;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_pago")
    private OffsetDateTime fechaPago;

    public Pago() {
    }

	public Pago(UUID idPago, UUID idReserva, String referenciaStripe, String claveIdempotencia, BigDecimal monto,
			String estado, OffsetDateTime fechaCreacion, OffsetDateTime fechaPago) {
		super();
		this.idPago = idPago;
		this.idReserva = idReserva;
		this.referenciaStripe = referenciaStripe;
		this.claveIdempotencia = claveIdempotencia;
		this.monto = monto;
		this.estado = estado;
		this.fechaCreacion = fechaCreacion;
		this.fechaPago = fechaPago;
	}

	public UUID getIdPago() {
		return idPago;
	}

	public void setIdPago(UUID idPago) {
		this.idPago = idPago;
	}

	public UUID getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(UUID idReserva) {
		this.idReserva = idReserva;
	}

	public String getReferenciaStripe() {
		return referenciaStripe;
	}

	public void setReferenciaStripe(String referenciaStripe) {
		this.referenciaStripe = referenciaStripe;
	}

	public String getClaveIdempotencia() {
		return claveIdempotencia;
	}

	public void setClaveIdempotencia(String claveIdempotencia) {
		this.claveIdempotencia = claveIdempotencia;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public OffsetDateTime getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(OffsetDateTime fechaPago) {
		this.fechaPago = fechaPago;
	}

    
}