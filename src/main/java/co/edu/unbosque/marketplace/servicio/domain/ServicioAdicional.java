package co.edu.unbosque.marketplace.servicio.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "servicio_adicional")
public class ServicioAdicional {

    @Id
    @Column(name = "id_adicional", nullable = false)
    private UUID idAdicional;

    @Column(name = "id_publicacion", nullable = false)
    private UUID idPublicacion;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "costo", nullable = false)
    private BigDecimal costo;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public ServicioAdicional() {
    }

	public ServicioAdicional(UUID idAdicional, UUID idPublicacion, String nombre, String descripcion, BigDecimal costo,
			boolean activo) {
		super();
		this.idAdicional = idAdicional;
		this.idPublicacion = idPublicacion;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.costo = costo;
		this.activo = activo;
	}

	public UUID getIdAdicional() {
		return idAdicional;
	}

	public void setIdAdicional(UUID idAdicional) {
		this.idAdicional = idAdicional;
	}

	public UUID getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(UUID idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

    
}