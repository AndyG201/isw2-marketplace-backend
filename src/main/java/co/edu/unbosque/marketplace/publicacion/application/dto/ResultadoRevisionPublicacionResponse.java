package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.util.UUID;

public abstract class ResultadoRevisionPublicacionResponse {

    private final UUID idRevision;
    private final UUID idPublicacion;
    private final String accion;
    private final String estadoAnterior;
    private final String estadoActual;
    private final String motivo;
    private final String mensaje;

    protected ResultadoRevisionPublicacionResponse(
            UUID idRevision,
            UUID idPublicacion,
            String accion,
            String estadoAnterior,
            String estadoActual,
            String motivo,
            String mensaje) {

        this.idRevision = idRevision;
        this.idPublicacion = idPublicacion;
        this.accion = accion;
        this.estadoAnterior = estadoAnterior;
        this.estadoActual = estadoActual;
        this.motivo = motivo;
        this.mensaje = mensaje;
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
}
