package co.edu.unbosque.marketplace.publicacion.api.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SeleccionServiciosAdicionalesResponse(
        UUID idPublicacion,
        List<ServicioAdicionalPublicacionResponse> serviciosSeleccionados,
        BigDecimal costoAdicionalTotal
) {
    public SeleccionServiciosAdicionalesResponse {
        serviciosSeleccionados = List.copyOf(serviciosSeleccionados);
    }
}