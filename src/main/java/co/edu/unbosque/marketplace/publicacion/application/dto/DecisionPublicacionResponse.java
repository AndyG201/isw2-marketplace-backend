package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DecisionPublicacionResponse extends ResultadoRevisionPublicacionResponse {

    private final OffsetDateTime fechaRevision;

    public DecisionPublicacionResponse(
            UUID idRevision,
            UUID idPublicacion,
            String accion,
            String estadoAnterior,
            String estadoActual,
            String motivo,
            String mensaje,
            OffsetDateTime fechaRevision) {

        super(idRevision, idPublicacion, accion, estadoAnterior, estadoActual, motivo, mensaje);
        this.fechaRevision = fechaRevision;
    }

    public OffsetDateTime getFechaRevision() {
        return fechaRevision;
    }
}
