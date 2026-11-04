package co.edu.unbosque.marketplace.publicacion.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "imagen_publicacion")
public class ImagenPublicacion {

    @Id
    @Column(name = "id_imagen", nullable = false)
    private UUID idImagen;

    @Column(name = "id_publicacion", nullable = false)
    private UUID idPublicacion;

    @Column(name = "ruta_imagen", length = 500, nullable = false)
    private String rutaImagen;

    @Column(name = "orden", nullable = false)
    private int orden;

    @Column(name = "es_portada", nullable = false)
    private boolean esPortada;

    public ImagenPublicacion() {
    }

	public ImagenPublicacion(UUID idImagen, UUID idPublicacion, String rutaImagen, int orden, boolean esPortada) {
		super();
		this.idImagen = idImagen;
		this.idPublicacion = idPublicacion;
		this.rutaImagen = rutaImagen;
		this.orden = orden;
		this.esPortada = esPortada;
	}

	public UUID getIdImagen() {
		return idImagen;
	}

	public void setIdImagen(UUID idImagen) {
		this.idImagen = idImagen;
	}

	public UUID getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(UUID idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public String getRutaImagen() {
		return rutaImagen;
	}

	public void setRutaImagen(String rutaImagen) {
		this.rutaImagen = rutaImagen;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	public boolean isEsPortada() {
		return esPortada;
	}

	public void setEsPortada(boolean esPortada) {
		this.esPortada = esPortada;
	}

    
}