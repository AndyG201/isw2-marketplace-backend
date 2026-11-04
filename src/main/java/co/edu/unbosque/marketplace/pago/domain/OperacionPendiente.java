package co.edu.unbosque.marketplace.pago.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "operacion_pendiente")
public class OperacionPendiente {

    @Id
    @Column(name = "id_operacion", nullable = false)
    private UUID idOperacion;

    @Column(name = "id_reserva", nullable = false)
    private UUID idReserva;

    @Column(name = "id_pago")
    private UUID idPago;

    @Column(name = "tipo_operacion", length = 40, nullable = false)
    private String tipoOperacion;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "proxima_ejecucion", nullable = false)
    private OffsetDateTime proximaEjecucion;

    @Column(name = "ultima_ejecucion")
    private OffsetDateTime ultimaEjecucion;

    @Column(name = "ultimo_error")
    private String ultimoError;

    @Column(name = "clave_idempotencia", length = 100, nullable = false)
    private String claveIdempotencia;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    public OperacionPendiente() {
    }

	public OperacionPendiente(UUID idOperacion, UUID idReserva, UUID idPago, String tipoOperacion, String estado,
			int intentos, OffsetDateTime proximaEjecucion, OffsetDateTime ultimaEjecucion, String ultimoError,
			String claveIdempotencia, OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
		super();
		this.idOperacion = idOperacion;
		this.idReserva = idReserva;
		this.idPago = idPago;
		this.tipoOperacion = tipoOperacion;
		this.estado = estado;
		this.intentos = intentos;
		this.proximaEjecucion = proximaEjecucion;
		this.ultimaEjecucion = ultimaEjecucion;
		this.ultimoError = ultimoError;
		this.claveIdempotencia = claveIdempotencia;
		this.fechaCreacion = fechaCreacion;
		this.fechaActualizacion = fechaActualizacion;
	}

	public UUID getIdOperacion() {
		return idOperacion;
	}

	public void setIdOperacion(UUID idOperacion) {
		this.idOperacion = idOperacion;
	}

	public UUID getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(UUID idReserva) {
		this.idReserva = idReserva;
	}

	public UUID getIdPago() {
		return idPago;
	}

	public void setIdPago(UUID idPago) {
		this.idPago = idPago;
	}

	public String getTipoOperacion() {
		return tipoOperacion;
	}

	public void setTipoOperacion(String tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public int getIntentos() {
		return intentos;
	}

	public void setIntentos(int intentos) {
		this.intentos = intentos;
	}

	public OffsetDateTime getProximaEjecucion() {
		return proximaEjecucion;
	}

	public void setProximaEjecucion(OffsetDateTime proximaEjecucion) {
		this.proximaEjecucion = proximaEjecucion;
	}

	public OffsetDateTime getUltimaEjecucion() {
		return ultimaEjecucion;
	}

	public void setUltimaEjecucion(OffsetDateTime ultimaEjecucion) {
		this.ultimaEjecucion = ultimaEjecucion;
	}

	public String getUltimoError() {
		return ultimoError;
	}

	public void setUltimoError(String ultimoError) {
		this.ultimoError = ultimoError;
	}

	public String getClaveIdempotencia() {
		return claveIdempotencia;
	}

	public void setClaveIdempotencia(String claveIdempotencia) {
		this.claveIdempotencia = claveIdempotencia;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public OffsetDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(OffsetDateTime fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

    
}