package co.edu.unbosque.marketplace.tarifa.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConfiguracionTarifaInfo(
        UUID idConfiguracion,
        BigDecimal porcentajeServicio,
        BigDecimal porcentajeDescuento,
        OffsetDateTime vigenteDesde
) {
}