package co.edu.unbosque.marketplace.publicacion.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "publicacion")
public class Publicacion {

    @Id
    @Column(name = "id_publicacion", nullable = false)
    private UUID idPublicacion;

    @Column(name = "id_alojamiento", nullable = false)
    private UUID idAlojamiento;

    @Column(name = "id_politica", nullable = false)
    private UUID idPolitica;

    @Column(name = "titulo", length = 200, nullable = false)
    private String titulo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "reglas", nullable = false)
    private String reglas;

    @Column(name = "precio_por_noche", nullable = false)
    private BigDecimal precioPorNoche;

    @Column(name = "tarifa_limpieza", nullable = false)
    private BigDecimal tarifaLimpieza;

    @Column(name = "modo_aceptacion", length = 30, nullable = false)
    private String modoAceptacion;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    public Publicacion() {
    }

	public Publicacion(UUID idPublicacion, UUID idAlojamiento, UUID idPolitica, String titulo, String descripcion,
			String reglas, BigDecimal precioPorNoche, BigDecimal tarifaLimpieza, String modoAceptacion, String estado,
			OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
		super();
		this.idPublicacion = idPublicacion;
		this.idAlojamiento = idAlojamiento;
		this.idPolitica = idPolitica;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.reglas = reglas;
		this.precioPorNoche = precioPorNoche;
		this.tarifaLimpieza = tarifaLimpieza;
		this.modoAceptacion = modoAceptacion;
		this.estado = estado;
		this.fechaCreacion = fechaCreacion;
		this.fechaActualizacion = fechaActualizacion;
	}

	public UUID getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(UUID idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public UUID getIdAlojamiento() {
		return idAlojamiento;
	}

	public void setIdAlojamiento(UUID idAlojamiento) {
		this.idAlojamiento = idAlojamiento;
	}

	public UUID getIdPolitica() {
		return idPolitica;
	}

	public void setIdPolitica(UUID idPolitica) {
		this.idPolitica = idPolitica;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getReglas() {
		return reglas;
	}

	public void setReglas(String reglas) {
		this.reglas = reglas;
	}

	public BigDecimal getPrecioPorNoche() {
		return precioPorNoche;
	}

	public void setPrecioPorNoche(BigDecimal precioPorNoche) {
		this.precioPorNoche = precioPorNoche;
	}

	public BigDecimal getTarifaLimpieza() {
		return tarifaLimpieza;
	}

	public void setTarifaLimpieza(BigDecimal tarifaLimpieza) {
		this.tarifaLimpieza = tarifaLimpieza;
	}

	public String getModoAceptacion() {
		return modoAceptacion;
	}

	public void setModoAceptacion(String modoAceptacion) {
		this.modoAceptacion = modoAceptacion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
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