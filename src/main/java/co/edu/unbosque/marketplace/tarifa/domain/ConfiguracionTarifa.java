package co.edu.unbosque.marketplace.tarifa.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion_tarifa")
public class ConfiguracionTarifa {

    @Id
    @Column(name = "id_configuracion", nullable = false)
    private UUID idConfiguracion;

    @Column(name = "id_administrador", nullable = false)
    private UUID idAdministrador;

    @Column(name = "porcentaje_servicio", nullable = false)
    private BigDecimal porcentajeServicio;

    @Column(name = "porcentaje_descuento", nullable = false)
    private BigDecimal porcentajeDescuento;

    @Column(name = "vigente_desde", nullable = false)
    private OffsetDateTime vigenteDesde;

    @Column(name = "vigente_hasta")
    private OffsetDateTime vigenteHasta;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    public ConfiguracionTarifa() {
    }

	public ConfiguracionTarifa(UUID idConfiguracion, UUID idAdministrador, BigDecimal porcentajeServicio,
			BigDecimal porcentajeDescuento, OffsetDateTime vigenteDesde, OffsetDateTime vigenteHasta,
			OffsetDateTime fechaCreacion) {
		super();
		this.idConfiguracion = idConfiguracion;
		this.idAdministrador = idAdministrador;
		this.porcentajeServicio = porcentajeServicio;
		this.porcentajeDescuento = porcentajeDescuento;
		this.vigenteDesde = vigenteDesde;
		this.vigenteHasta = vigenteHasta;
		this.fechaCreacion = fechaCreacion;
	}

	public UUID getIdConfiguracion() {
		return idConfiguracion;
	}

	public void setIdConfiguracion(UUID idConfiguracion) {
		this.idConfiguracion = idConfiguracion;
	}

	public UUID getIdAdministrador() {
		return idAdministrador;
	}

	public void setIdAdministrador(UUID idAdministrador) {
		this.idAdministrador = idAdministrador;
	}

	public BigDecimal getPorcentajeServicio() {
		return porcentajeServicio;
	}

	public void setPorcentajeServicio(BigDecimal porcentajeServicio) {
		this.porcentajeServicio = porcentajeServicio;
	}

	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		this.porcentajeDescuento = porcentajeDescuento;
	}

	public OffsetDateTime getVigenteDesde() {
		return vigenteDesde;
	}

	public void setVigenteDesde(OffsetDateTime vigenteDesde) {
		this.vigenteDesde = vigenteDesde;
	}

	public OffsetDateTime getVigenteHasta() {
		return vigenteHasta;
	}

	public void setVigenteHasta(OffsetDateTime vigenteHasta) {
		this.vigenteHasta = vigenteHasta;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

   
}