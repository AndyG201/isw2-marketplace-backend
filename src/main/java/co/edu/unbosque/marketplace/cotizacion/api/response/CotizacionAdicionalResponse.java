package co.edu.unbosque.marketplace.cotizacion.api.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CotizacionAdicionalResponse(
        UUID idAdicional,
        String nombre,
        int cantidad,
        BigDecimal precioAplicado,
        BigDecimal subtotal
) {
}