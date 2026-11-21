package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.util.List;

public record PublicacionAdministracionResponse(
        PublicacionDetalleResponse publicacion,
        List<ServicioAdicionalPublicacionResponse> serviciosAdicionales,
        PoliticaCancelacionPublicacionResponse politica,
        SolicitudCambioResponse solicitudCambios
) {

    public PublicacionAdministracionResponse {
        serviciosAdicionales = List.copyOf(serviciosAdicionales);
    }
}
