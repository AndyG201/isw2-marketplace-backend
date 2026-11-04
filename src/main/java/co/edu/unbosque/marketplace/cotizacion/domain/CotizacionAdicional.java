package co.edu.unbosque.marketplace.cotizacion.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cotizacion_adicional")
public class CotizacionAdicional {

    @Id
    @Column(name = "id_cotizacion", nullable = false)
    private UUID idCotizacion;

    @Column(name = "id_adicional", nullable = false)
    private UUID idAdicional;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "precio_aplicado", nullable = false)
    private BigDecimal precioAplicado;

    public CotizacionAdicional() {
    }

	public CotizacionAdicional(UUID idCotizacion, UUID idAdicional, int cantidad, BigDecimal precioAplicado) {
		super();
		this.idCotizacion = idCotizacion;
		this.idAdicional = idAdicional;
		this.cantidad = cantidad;
		this.precioAplicado = precioAplicado;
	}

	public UUID getIdCotizacion() {
		return idCotizacion;
	}

	public void setIdCotizacion(UUID idCotizacion) {
		this.idCotizacion = idCotizacion;
	}

	public UUID getIdAdicional() {
		return idAdicional;
	}

	public void setIdAdicional(UUID idAdicional) {
		this.idAdicional = idAdicional;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public BigDecimal getPrecioAplicado() {
		return precioAplicado;
	}

	public void setPrecioAplicado(BigDecimal precioAplicado) {
		this.precioAplicado = precioAplicado;
	}

    
}