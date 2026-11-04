package co.edu.unbosque.marketplace.cancelacion.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cancelacion")
public class Cancelacion {

    @Id
    @Column(name = "id_cancelacion", nullable = false)
    private UUID idCancelacion;

    @Column(name = "id_reserva", nullable = false)
    private UUID idReserva;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "fecha_cancelacion", nullable = false)
    private OffsetDateTime fechaCancelacion;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Column(name = "valor_devolucion", nullable = false)
    private BigDecimal valorDevolucion;

    @Column(name = "estado_devolucion", length = 30, nullable = false)
    private String estadoDevolucion;

    @Column(name = "estado_final", length = 30, nullable = false)
    private String estadoFinal;

    public Cancelacion() {
    }

	public Cancelacion(UUID idCancelacion, UUID idReserva, UUID idUsuario, OffsetDateTime fechaCancelacion,
			String motivo, BigDecimal valorDevolucion, String estadoDevolucion, String estadoFinal) {
		super();
		this.idCancelacion = idCancelacion;
		this.idReserva = idReserva;
		this.idUsuario = idUsuario;
		this.fechaCancelacion = fechaCancelacion;
		this.motivo = motivo;
		this.valorDevolucion = valorDevolucion;
		this.estadoDevolucion = estadoDevolucion;
		this.estadoFinal = estadoFinal;
	}

	public UUID getIdCancelacion() {
		return idCancelacion;
	}

	public void setIdCancelacion(UUID idCancelacion) {
		this.idCancelacion = idCancelacion;
	}

	public UUID getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(UUID idReserva) {
		this.idReserva = idReserva;
	}

	public UUID getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(UUID idUsuario) {
		this.idUsuario = idUsuario;
	}

	public OffsetDateTime getFechaCancelacion() {
		return fechaCancelacion;
	}

	public void setFechaCancelacion(OffsetDateTime fechaCancelacion) {
		this.fechaCancelacion = fechaCancelacion;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public BigDecimal getValorDevolucion() {
		return valorDevolucion;
	}

	public void setValorDevolucion(BigDecimal valorDevolucion) {
		this.valorDevolucion = valorDevolucion;
	}

	public String getEstadoDevolucion() {
		return estadoDevolucion;
	}

	public void setEstadoDevolucion(String estadoDevolucion) {
		this.estadoDevolucion = estadoDevolucion;
	}

	public String getEstadoFinal() {
		return estadoFinal;
	}

	public void setEstadoFinal(String estadoFinal) {
		this.estadoFinal = estadoFinal;
	}

    
}