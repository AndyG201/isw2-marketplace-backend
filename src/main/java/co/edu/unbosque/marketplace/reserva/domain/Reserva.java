package co.edu.unbosque.marketplace.reserva.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @Column(name = "id_reserva", nullable = false)
    private UUID idReserva;

    @Column(name = "id_huesped", nullable = false)
    private UUID idHuesped;

    @Column(name = "id_cotizacion", nullable = false)
    private UUID idCotizacion;

    @Column(name = "id_politica_aplicada", nullable = false)
    private UUID idPoliticaAplicada;

    @Column(name = "modo_aceptacion", length = 30, nullable = false)
    private String modoAceptacion;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "retencion_hasta")
    private OffsetDateTime retencionHasta;

    @Column(name = "clave_idempotencia", length = 100, nullable = false)
    private String claveIdempotencia;

    @Column(name = "requiere_revision_manual", nullable = false)
    private boolean requiereRevisionManual;

    @Column(name = "fecha_solicitud", nullable = false)
    private OffsetDateTime fechaSolicitud;

    @Column(name = "fecha_confirmacion")
    private OffsetDateTime fechaConfirmacion;

    public Reserva() {
    }

	public Reserva(UUID idReserva, UUID idHuesped, UUID idCotizacion, UUID idPoliticaAplicada, String modoAceptacion,
			String estado, OffsetDateTime retencionHasta, String claveIdempotencia, boolean requiereRevisionManual,
			OffsetDateTime fechaSolicitud, OffsetDateTime fechaConfirmacion) {
		super();
		this.idReserva = idReserva;
		this.idHuesped = idHuesped;
		this.idCotizacion = idCotizacion;
		this.idPoliticaAplicada = idPoliticaAplicada;
		this.modoAceptacion = modoAceptacion;
		this.estado = estado;
		this.retencionHasta = retencionHasta;
		this.claveIdempotencia = claveIdempotencia;
		this.requiereRevisionManual = requiereRevisionManual;
		this.fechaSolicitud = fechaSolicitud;
		this.fechaConfirmacion = fechaConfirmacion;
	}

	public UUID getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(UUID idReserva) {
		this.idReserva = idReserva;
	}

	public UUID getIdHuesped() {
		return idHuesped;
	}

	public void setIdHuesped(UUID idHuesped) {
		this.idHuesped = idHuesped;
	}

	public UUID getIdCotizacion() {
		return idCotizacion;
	}

	public void setIdCotizacion(UUID idCotizacion) {
		this.idCotizacion = idCotizacion;
	}

	public UUID getIdPoliticaAplicada() {
		return idPoliticaAplicada;
	}

	public void setIdPoliticaAplicada(UUID idPoliticaAplicada) {
		this.idPoliticaAplicada = idPoliticaAplicada;
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

	public OffsetDateTime getRetencionHasta() {
		return retencionHasta;
	}

	public void setRetencionHasta(OffsetDateTime retencionHasta) {
		this.retencionHasta = retencionHasta;
	}

	public String getClaveIdempotencia() {
		return claveIdempotencia;
	}

	public void setClaveIdempotencia(String claveIdempotencia) {
		this.claveIdempotencia = claveIdempotencia;
	}

	public boolean isRequiereRevisionManual() {
		return requiereRevisionManual;
	}

	public void setRequiereRevisionManual(boolean requiereRevisionManual) {
		this.requiereRevisionManual = requiereRevisionManual;
	}

	public OffsetDateTime getFechaSolicitud() {
		return fechaSolicitud;
	}

	public void setFechaSolicitud(OffsetDateTime fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}

	public OffsetDateTime getFechaConfirmacion() {
		return fechaConfirmacion;
	}

	public void setFechaConfirmacion(OffsetDateTime fechaConfirmacion) {
		this.fechaConfirmacion = fechaConfirmacion;
	}

    
}