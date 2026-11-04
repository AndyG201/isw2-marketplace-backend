package co.edu.unbosque.marketplace.publicacion.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "publicacion_servicio")
public class PublicacionServicio {

    @Id
    @Column(name = "id_publicacion", nullable = false)
    private UUID idPublicacion;

    @Column(name = "id_servicio", nullable = false)
    private UUID idServicio;

    public PublicacionServicio() {
    }

	public PublicacionServicio(UUID idPublicacion, UUID idServicio) {
		super();
		this.idPublicacion = idPublicacion;
		this.idServicio = idServicio;
	}

	public UUID getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(UUID idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public UUID getIdServicio() {
		return idServicio;
	}

	public void setIdServicio(UUID idServicio) {
		this.idServicio = idServicio;
	}

    
}