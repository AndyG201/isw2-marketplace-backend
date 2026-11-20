package co.edu.unbosque.marketplace.publicacion.api.response;

import java.time.OffsetDateTime;
import java.util.List;

public record SolicitudCambioResponse(
        String mensaje,
        List<String> campos,
        String estadoAnterior,
        OffsetDateTime fechaSolicitud
) {

    public SolicitudCambioResponse {
        campos = List.copyOf(campos);
    }
}
