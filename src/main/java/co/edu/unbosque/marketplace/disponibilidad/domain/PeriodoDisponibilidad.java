package co.edu.unbosque.marketplace.disponibilidad.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "periodo_disponibilidad")
public class PeriodoDisponibilidad {

    @Id
    @Column(name = "id_periodo", nullable = false)
    private UUID idPeriodo;

    @Column(name = "id_alojamiento", nullable = false)
    private UUID idAlojamiento;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "habilitado", nullable = false)
    private boolean habilitado;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    public PeriodoDisponibilidad() {
    }

	public PeriodoDisponibilidad(UUID idPeriodo, UUID idAlojamiento, LocalDate fechaInicio, LocalDate fechaFin,
			boolean habilitado, OffsetDateTime fechaCreacion) {
		super();
		this.idPeriodo = idPeriodo;
		this.idAlojamiento = idAlojamiento;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.habilitado = habilitado;
		this.fechaCreacion = fechaCreacion;
	}

	public UUID getIdPeriodo() {
		return idPeriodo;
	}

	public void setIdPeriodo(UUID idPeriodo) {
		this.idPeriodo = idPeriodo;
	}

	public UUID getIdAlojamiento() {
		return idAlojamiento;
	}

	public void setIdAlojamiento(UUID idAlojamiento) {
		this.idAlojamiento = idAlojamiento;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public boolean isHabilitado() {
		return habilitado;
	}

	public void setHabilitado(boolean habilitado) {
		this.habilitado = habilitado;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

    
}