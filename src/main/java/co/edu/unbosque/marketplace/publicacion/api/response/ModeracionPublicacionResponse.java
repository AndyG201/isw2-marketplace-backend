package co.edu.unbosque.marketplace.publicacion.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ModeracionPublicacionResponse {

    private UUID idRevision;
    private UUID idPublicacion;
    private String accion;
    private String estadoAnterior;
    private String estadoActual;
    private String motivo;
    private String mensaje;
    private OffsetDateTime fechaOperacion;

    public ModeracionPublicacionResponse(
            UUID idRevision,
            UUID idPublicacion,
            String accion,
            String estadoAnterior,
            String estadoActual,
            String motivo,
            String mensaje,
            OffsetDateTime fechaOperacion) {

        this.idRevision = idRevision;
        this.idPublicacion = idPublicacion;
        this.accion = accion;
        this.estadoAnterior = estadoAnterior;
        this.estadoActual = estadoActual;
        this.motivo = motivo;
        this.mensaje = mensaje;
        this.fechaOperacion = fechaOperacion;
    }

    public UUID getIdRevision() {
        return idRevision;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public String getAccion() {
        return accion;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public OffsetDateTime getFechaOperacion() {
        return fechaOperacion;
    }
}
