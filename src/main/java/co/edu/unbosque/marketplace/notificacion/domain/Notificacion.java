package co.edu.unbosque.marketplace.notificacion.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @Column(name = "id_notificacion", nullable = false)
    private UUID idNotificacion;

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "id_reserva")
    private UUID idReserva;

    @Column(name = "id_publicacion")
    private UUID idPublicacion;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_lectura")
    private OffsetDateTime fechaLectura;

    public Notificacion() {
    }

	public Notificacion(UUID idNotificacion, UUID idUsuario, UUID idReserva, UUID idPublicacion, String tipo,
			String mensaje, OffsetDateTime fechaCreacion, OffsetDateTime fechaLectura) {
		super();
		this.idNotificacion = idNotificacion;
		this.idUsuario = idUsuario;
		this.idReserva = idReserva;
		this.idPublicacion = idPublicacion;
		this.tipo = tipo;
		this.mensaje = mensaje;
		this.fechaCreacion = fechaCreacion;
		this.fechaLectura = fechaLectura;
	}

	public UUID getIdNotificacion() {
		return idNotificacion;
	}

	public void setIdNotificacion(UUID idNotificacion) {
		this.idNotificacion = idNotificacion;
	}

	public UUID getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(UUID idUsuario) {
		this.idUsuario = idUsuario;
	}

	public UUID getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(UUID idReserva) {
		this.idReserva = idReserva;
	}

	public UUID getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(UUID idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public OffsetDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(OffsetDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public OffsetDateTime getFechaLectura() {
		return fechaLectura;
	}

	public void setFechaLectura(OffsetDateTime fechaLectura) {
		this.fechaLectura = fechaLectura;
	}

   
}