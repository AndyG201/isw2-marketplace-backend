package co.edu.unbosque.marketplace.publicacion.api.response;

import java.util.List;

public record PublicacionPublicaResponse(
        PublicacionDetalleResponse publicacion,
        List<ServicioAdicionalPublicacionResponse> serviciosAdicionales,
        PoliticaCancelacionPublicacionResponse politica
) {
}
