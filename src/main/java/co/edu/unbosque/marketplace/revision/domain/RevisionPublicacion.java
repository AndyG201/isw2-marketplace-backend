package co.edu.unbosque.marketplace.revision.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "revision_publicacion")
public class RevisionPublicacion {

    @Id
    @Column(name = "id_revision", nullable = false)
    private UUID idRevision;

    @Column(name = "id_publicacion", nullable = false)
    private UUID idPublicacion;

    @Column(name = "id_administrador", nullable = false)
    private UUID idAdministrador;

    @Column(name = "accion", length = 50, nullable = false)
    private String accion;

    @Column(name = "resultado", length = 30, nullable = false)
    private String resultado;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "fecha_revision", nullable = false)
    private OffsetDateTime fechaRevision;

    public RevisionPublicacion() {
    }

	public RevisionPublicacion(UUID idRevision, UUID idPublicacion, UUID idAdministrador, String accion,
			String resultado, String motivo, OffsetDateTime fechaRevision) {
		super();
		this.idRevision = idRevision;
		this.idPublicacion = idPublicacion;
		this.idAdministrador = idAdministrador;
		this.accion = accion;
		this.resultado = resultado;
		this.motivo = motivo;
		this.fechaRevision = fechaRevision;
	}

	public UUID getIdRevision() {
		return idRevision;
	}

	public void setIdRevision(UUID idRevision) {
		this.idRevision = idRevision;
	}

	public UUID getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(UUID idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public UUID getIdAdministrador() {
		return idAdministrador;
	}

	public void setIdAdministrador(UUID idAdministrador) {
		this.idAdministrador = idAdministrador;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public OffsetDateTime getFechaRevision() {
		return fechaRevision;
	}

	public void setFechaRevision(OffsetDateTime fechaRevision) {
		this.fechaRevision = fechaRevision;
	}

    
}