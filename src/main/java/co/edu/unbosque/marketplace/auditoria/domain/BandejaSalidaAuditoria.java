package co.edu.unbosque.marketplace.auditoria.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnTransformer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bandeja_salida_auditoria")
public class BandejaSalidaAuditoria {

    @Id
    @Column(name = "id_evento", nullable = false)
    private UUID idEvento;

    @Column(name = "id_usuario")
    private UUID idUsuario;

    @Column(name = "fecha_evento", nullable = false)
    private OffsetDateTime fechaEvento;

    @Column(name = "accion", length = 100, nullable = false)
    private String accion;

    @Column(name = "resultado", length = 30, nullable = false)
    private String resultado;

    @Column(name = "entidad_afectada", length = 50, nullable = false)
    private String entidadAfectada;

    @Column(name = "id_entidad_afectada")
    private UUID idEntidadAfectada;

    @Column(name = "detalle", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String detalle;

    @Column(name = "id_correlacion", length = 100)
    private String idCorrelacion;

    @Column(name = "estado_envio", length = 20, nullable = false)
    private String estadoEnvio;

    @Column(name = "intentos_envio", nullable = false)
    private int intentosEnvio;

    @Column(name = "proxima_ejecucion", nullable = false)
    private OffsetDateTime proximaEjecucion;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;

    @Column(name = "ultimo_error")
    private String ultimoError;

    public BandejaSalidaAuditoria() {
    }

	public BandejaSalidaAuditoria(UUID idEvento, UUID idUsuario, OffsetDateTime fechaEvento, String accion,
			String resultado, String entidadAfectada, UUID idEntidadAfectada, String detalle, String idCorrelacion,
			String estadoEnvio, int intentosEnvio, OffsetDateTime proximaEjecucion, OffsetDateTime fechaEnvio,
			String ultimoError) {
		super();
		this.idEvento = idEvento;
		this.idUsuario = idUsuario;
		this.fechaEvento = fechaEvento;
		this.accion = accion;
		this.resultado = resultado;
		this.entidadAfectada = entidadAfectada;
		this.idEntidadAfectada = idEntidadAfectada;
		this.detalle = detalle;
		this.idCorrelacion = idCorrelacion;
		this.estadoEnvio = estadoEnvio;
		this.intentosEnvio = intentosEnvio;
		this.proximaEjecucion = proximaEjecucion;
		this.fechaEnvio = fechaEnvio;
		this.ultimoError = ultimoError;
	}

	public UUID getIdEvento() {
		return idEvento;
	}

	public void setIdEvento(UUID idEvento) {
		this.idEvento = idEvento;
	}

	public UUID getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(UUID idUsuario) {
		this.idUsuario = idUsuario;
	}

	public OffsetDateTime getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(OffsetDateTime fechaEvento) {
		this.fechaEvento = fechaEvento;
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

	public String getEntidadAfectada() {
		return entidadAfectada;
	}

	public void setEntidadAfectada(String entidadAfectada) {
		this.entidadAfectada = entidadAfectada;
	}

	public UUID getIdEntidadAfectada() {
		return idEntidadAfectada;
	}

	public void setIdEntidadAfectada(UUID idEntidadAfectada) {
		this.idEntidadAfectada = idEntidadAfectada;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public String getIdCorrelacion() {
		return idCorrelacion;
	}

	public void setIdCorrelacion(String idCorrelacion) {
		this.idCorrelacion = idCorrelacion;
	}

	public String getEstadoEnvio() {
		return estadoEnvio;
	}

	public void setEstadoEnvio(String estadoEnvio) {
		this.estadoEnvio = estadoEnvio;
	}

	public int getIntentosEnvio() {
		return intentosEnvio;
	}

	public void setIntentosEnvio(int intentosEnvio) {
		this.intentosEnvio = intentosEnvio;
	}

	public OffsetDateTime getProximaEjecucion() {
		return proximaEjecucion;
	}

	public void setProximaEjecucion(OffsetDateTime proximaEjecucion) {
		this.proximaEjecucion = proximaEjecucion;
	}

	public OffsetDateTime getFechaEnvio() {
		return fechaEnvio;
	}

	public void setFechaEnvio(OffsetDateTime fechaEnvio) {
		this.fechaEnvio = fechaEnvio;
	}

	public String getUltimoError() {
		return ultimoError;
	}

	public void setUltimoError(String ultimoError) {
		this.ultimoError = ultimoError;
	}

    
}