package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class EnviarRevisionResponse extends EstadoPublicacionResponse {

    public EnviarRevisionResponse(
            UUID idPublicacion,
            String estadoAnterior,
            String estadoActual,
            String mensaje,
            OffsetDateTime fechaActualizacion) {
        super(idPublicacion, estadoAnterior, estadoActual, mensaje, fechaActualizacion);
    }
}
