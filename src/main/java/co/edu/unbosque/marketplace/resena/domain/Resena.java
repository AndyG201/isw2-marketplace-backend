package co.edu.unbosque.marketplace.resena.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resena")
public class Resena {

    @Id
    @Column(name = "id_resena", nullable = false)
    private UUID idResena;

    @Column(name = "id_reserva", nullable = false)
    private UUID idReserva;

    @Column(name = "calificacion", nullable = false)
    private int calificacion;

    @Column(name = "comentario", nullable = false)
    private String comentario;

    @Column(name = "visible", nullable = false)
    private boolean visible;

    @Column(name = "fecha_publicacion", nullable = false)
    private OffsetDateTime fechaPublicacion;

    @Column(name = "id_administrador_moderador")
    private UUID idAdministradorModerador;

    @Column(name = "motivo_ocultamiento")
    private String motivoOcultamiento;

    @Column(name = "fecha_ocultamiento")
    private OffsetDateTime fechaOcultamiento;

    public Resena() {
    }

	public Resena(UUID idResena, UUID idReserva, int calificacion, String comentario, boolean visible,
			OffsetDateTime fechaPublicacion, UUID idAdministradorModerador, String motivoOcultamiento,
			OffsetDateTime fechaOcultamiento) {
		super();
		this.idResena = idResena;
		this.idReserva = idReserva;
		this.calificacion = calificacion;
		this.comentario = comentario;
		this.visible = visible;
		this.fechaPublicacion = fechaPublicacion;
		this.idAdministradorModerador = idAdministradorModerador;
		this.motivoOcultamiento = motivoOcultamiento;
		this.fechaOcultamiento = fechaOcultamiento;
	}

	public UUID getIdResena() {
		return idResena;
	}

	public void setIdResena(UUID idResena) {
		this.idResena = idResena;
	}

	public UUID getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(UUID idReserva) {
		this.idReserva = idReserva;
	}

	public int getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(int calificacion) {
		this.calificacion = calificacion;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public OffsetDateTime getFechaPublicacion() {
		return fechaPublicacion;
	}

	public void setFechaPublicacion(OffsetDateTime fechaPublicacion) {
		this.fechaPublicacion = fechaPublicacion;
	}

	public UUID getIdAdministradorModerador() {
		return idAdministradorModerador;
	}

	public void setIdAdministradorModerador(UUID idAdministradorModerador) {
		this.idAdministradorModerador = idAdministradorModerador;
	}

	public String getMotivoOcultamiento() {
		return motivoOcultamiento;
	}

	public void setMotivoOcultamiento(String motivoOcultamiento) {
		this.motivoOcultamiento = motivoOcultamiento;
	}

	public OffsetDateTime getFechaOcultamiento() {
		return fechaOcultamiento;
	}

	public void setFechaOcultamiento(OffsetDateTime fechaOcultamiento) {
		this.fechaOcultamiento = fechaOcultamiento;
	}


}