package co.edu.unbosque.marketplace.cotizacion.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cotizacion")
public class Cotizacion {

    @Id
    @Column(name = "id_cotizacion", nullable = false)
    private UUID idCotizacion;

    @Column(name = "id_publicacion", nullable = false)
    private UUID idPublicacion;

    @Column(name = "id_configuracion", nullable = false)
    private UUID idConfiguracion;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "cantidad_huespedes", nullable = false)
    private int cantidadHuespedes;

    @Column(name = "precio_noche_aplicado", nullable = false)
    private BigDecimal precioNocheAplicado;

    @Column(name = "tarifa_limpieza", nullable = false)
    private BigDecimal tarifaLimpieza;

    @Column(name = "fecha_cotizacion", nullable = false)
    private OffsetDateTime fechaCotizacion;

    public Cotizacion() {
    }

	public Cotizacion(UUID idCotizacion, UUID idPublicacion, UUID idConfiguracion, LocalDate fechaEntrada,
			LocalDate fechaSalida, int cantidadHuespedes, BigDecimal precioNocheAplicado, BigDecimal tarifaLimpieza,
			OffsetDateTime fechaCotizacion) {
		super();
		this.idCotizacion = idCotizacion;
		this.idPublicacion = idPublicacion;
		this.idConfiguracion = idConfiguracion;
		this.fechaEntrada = fechaEntrada;
		this.fechaSalida = fechaSalida;
		this.cantidadHuespedes = cantidadHuespedes;
		this.precioNocheAplicado = precioNocheAplicado;
		this.tarifaLimpieza = tarifaLimpieza;
		this.fechaCotizacion = fechaCotizacion;
	}

	public UUID getIdCotizacion() {
		return idCotizacion;
	}

	public void setIdCotizacion(UUID idCotizacion) {
		this.idCotizacion = idCotizacion;
	}

	public UUID getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(UUID idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public UUID getIdConfiguracion() {
		return idConfiguracion;
	}

	public void setIdConfiguracion(UUID idConfiguracion) {
		this.idConfiguracion = idConfiguracion;
	}

	public LocalDate getFechaEntrada() {
		return fechaEntrada;
	}

	public void setFechaEntrada(LocalDate fechaEntrada) {
		this.fechaEntrada = fechaEntrada;
	}

	public LocalDate getFechaSalida() {
		return fechaSalida;
	}

	public void setFechaSalida(LocalDate fechaSalida) {
		this.fechaSalida = fechaSalida;
	}

	public int getCantidadHuespedes() {
		return cantidadHuespedes;
	}

	public void setCantidadHuespedes(int cantidadHuespedes) {
		this.cantidadHuespedes = cantidadHuespedes;
	}

	public BigDecimal getPrecioNocheAplicado() {
		return precioNocheAplicado;
	}

	public void setPrecioNocheAplicado(BigDecimal precioNocheAplicado) {
		this.precioNocheAplicado = precioNocheAplicado;
	}

	public BigDecimal getTarifaLimpieza() {
		return tarifaLimpieza;
	}

	public void setTarifaLimpieza(BigDecimal tarifaLimpieza) {
		this.tarifaLimpieza = tarifaLimpieza;
	}

	public OffsetDateTime getFechaCotizacion() {
		return fechaCotizacion;
	}

	public void setFechaCotizacion(OffsetDateTime fechaCotizacion) {
		this.fechaCotizacion = fechaCotizacion;
	}

    
}