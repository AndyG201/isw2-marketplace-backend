package co.edu.unbosque.marketplace.servicio.contracts;

import java.math.BigDecimal;

public record ServicioAdicionalData(
        String nombre,
        String descripcion,
        BigDecimal costo
) {
}
