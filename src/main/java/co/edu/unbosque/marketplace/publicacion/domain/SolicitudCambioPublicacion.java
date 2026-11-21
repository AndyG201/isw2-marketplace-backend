package co.edu.unbosque.marketplace.publicacion.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitud_cambio_publicacion", schema = "public")
public class SolicitudCambioPublicacion {

    @Id
    @Column(name = "id_solicitud", nullable = false)
    private UUID idSolicitud;

    @Column(name = "id_publicacion", nullable = false)
    private UUID idPublicacion;

    @Column(name = "id_anfitrion", nullable = false)
    private UUID idAnfitrion;

    @Column(name = "mensaje", columnDefinition = "text", nullable = false)
    private String mensaje;

    @Column(name = "campos", columnDefinition = "text", nullable = false)
    private String campos;

    @Column(name = "estado_anterior", length = 30, nullable = false)
    private String estadoAnterior;

    @Column(name = "fecha_solicitud", nullable = false)
    private OffsetDateTime fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private OffsetDateTime fechaResolucion;

    public SolicitudCambioPublicacion() {
    }

    public SolicitudCambioPublicacion(UUID idSolicitud, UUID idPublicacion, UUID idAnfitrion, String mensaje,
            String campos, String estadoAnterior, OffsetDateTime fechaSolicitud) {
        this.idSolicitud = idSolicitud;
        this.idPublicacion = idPublicacion;
        this.idAnfitrion = idAnfitrion;
        this.mensaje = mensaje;
        this.campos = campos;
        this.estadoAnterior = estadoAnterior;
        this.fechaSolicitud = fechaSolicitud;
    }

    public UUID getIdSolicitud() {
        return idSolicitud;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public UUID getIdAnfitrion() {
        return idAnfitrion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getCampos() {
        return campos;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public OffsetDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public OffsetDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(OffsetDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }
}
