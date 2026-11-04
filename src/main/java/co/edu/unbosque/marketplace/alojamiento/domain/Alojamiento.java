package co.edu.unbosque.marketplace.alojamiento.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alojamiento")
public class Alojamiento {

    @Id
    @Column(name = "id_alojamiento", nullable = false)
    private UUID idAlojamiento;

    @Column(name = "id_anfitrion", nullable = false)
    private UUID idAnfitrion;

    @Column(name = "ubicacion", length = 255, nullable = false)
    private String ubicacion;

    @Column(name = "ciudad", length = 100, nullable = false)
    private String ciudad;

    @Column(name = "tipo_alojamiento", length = 50, nullable = false)
    private String tipoAlojamiento;

    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    public Alojamiento() {
    }

	public Alojamiento(UUID idAlojamiento, UUID idAnfitrion, String ubicacion, String ciudad, String tipoAlojamiento,
			int capacidad, OffsetDateTime fechaCreacion) {
		super();
		this.idAlojamiento = idAlojamiento;
		this.idAnfitrion = idAnfitrion;
		this.ubicacion = ubicacion;
		this.ciudad = ciudad;
		this.tipoAlojamiento = tipoAlojamiento;
		this.capacidad = capacidad;
		this.fechaCreacion = fechaCreacion;
	}

	public UUID getIdAlojamiento() {
		return idAlojamiento;
	}

	public void setIdAlojamiento(UUID idAlojamiento) {
		this.idAlojamiento = idAlojamiento;
	}

	public UUID getIdAnfitrion() {
		return idAnfitrion;
	}

	public void setIdAnfitrion(UUID idAnfitrion) {
		this.idAnfitrion = idAnfitrion;
	}

	public String getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(String ubicacion) {
		this.ubicacion = ubicacion;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public String getTipoAlojamiento() {
		return tipoAlojamiento;
	}

	public void setTipoAlojamiento(String tipoAlojamiento) {
		this.tipoAlojamiento = tipoAlojamiento;
	}

	public int getCapacidad() {
		return capacidad;
	}

	public void setCapacidad(int capacidad) {
		this.capacidad = capacidad;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

    
}