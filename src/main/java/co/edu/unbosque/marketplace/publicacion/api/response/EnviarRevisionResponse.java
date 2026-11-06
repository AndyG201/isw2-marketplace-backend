
package co.edu.unbosque.marketplace.publicacion.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class EnviarRevisionResponse {

    private UUID idPublicacion;
    private String estadoAnterior;
    private String estadoActual;
    private String mensaje;
    private OffsetDateTime fechaActualizacion;

    public EnviarRevisionResponse(
            UUID idPublicacion,
            String estadoAnterior,
            String estadoActual,
            String mensaje,
            OffsetDateTime fechaActualizacion) {

        this.idPublicacion = idPublicacion;
        this.estadoAnterior = estadoAnterior;
        this.estadoActual = estadoActual;
        this.mensaje = mensaje;
        this.fechaActualizacion = fechaActualizacion;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public String getMensaje() {
        return mensaje;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
