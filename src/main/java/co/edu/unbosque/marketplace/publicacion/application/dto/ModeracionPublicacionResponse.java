package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ModeracionPublicacionResponse extends ResultadoRevisionPublicacionResponse {

    private final OffsetDateTime fechaOperacion;

    public ModeracionPublicacionResponse(
            UUID idRevision,
            UUID idPublicacion,
            String accion,
            String estadoAnterior,
            String estadoActual,
            String motivo,
            String mensaje,
            OffsetDateTime fechaOperacion) {

        super(idRevision, idPublicacion, accion, estadoAnterior, estadoActual, motivo, mensaje);
        this.fechaOperacion = fechaOperacion;
    }

    public OffsetDateTime getFechaOperacion() {
        return fechaOperacion;
    }
}
