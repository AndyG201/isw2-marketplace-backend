package co.edu.unbosque.marketplace.tarifa.api.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record ConfigurarPorcentajesTarifaRequest(

        @NotNull(message = "El porcentaje de servicio es obligatorio.")
        @Digits(integer = 3, fraction = 2,
                message = "El porcentaje de servicio admite máximo dos decimales.")
        @DecimalMin(value = "0.00",
                message = "El porcentaje de servicio no puede ser menor que 0.")
        @DecimalMax(value = "100.00",
                message = "El porcentaje de servicio no puede superar 100.")
        BigDecimal porcentajeServicio,

        @NotNull(message = "El porcentaje de descuento es obligatorio.")
        @Digits(integer = 3, fraction = 2,
                message = "El porcentaje de descuento admite máximo dos decimales.")
        @DecimalMin(value = "0.00",
                message = "El porcentaje de descuento no puede ser menor que 0.")
        @DecimalMax(value = "100.00",
                message = "El porcentaje de descuento no puede superar 100.")
        BigDecimal porcentajeDescuento) {
}