package co.edu.unbosque.marketplace.tarifa.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConfiguracionTarifaResponse(
        UUID idConfiguracion,
        UUID idAdministrador,
        BigDecimal porcentajeServicio,
        BigDecimal porcentajeDescuento,
        OffsetDateTime vigenteDesde,
        OffsetDateTime vigenteHasta,
        OffsetDateTime fechaCreacion) {
}