package co.edu.unbosque.marketplace.cotizacion.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CotizacionResponse(
        UUID idCotizacion,
        UUID idPublicacion,
        UUID idConfiguracion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        long noches,
        int cantidadHuespedes,
        BigDecimal precioPorNoche,
        BigDecimal subtotalAlojamiento,
        BigDecimal tarifaLimpieza,
        BigDecimal porcentajeServicio,
        BigDecimal costoServicio,
        BigDecimal porcentajeDescuento,
        BigDecimal montoDescuento,
        List<CotizacionAdicionalResponse> serviciosAdicionales,
        BigDecimal costoAdicionalTotal,
        BigDecimal total,
        OffsetDateTime fechaCotizacion
) {
    public CotizacionResponse {
        serviciosAdicionales = List.copyOf(serviciosAdicionales);
    }
}