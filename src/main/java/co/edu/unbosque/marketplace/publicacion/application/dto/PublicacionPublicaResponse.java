package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.util.List;

public record PublicacionPublicaResponse(
        PublicacionDetalleResponse publicacion,
        List<ServicioAdicionalPublicacionResponse> serviciosAdicionales,
        PoliticaCancelacionPublicacionResponse politica
) {
}
