package co.edu.unbosque.marketplace.cotizacion.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicacionCotizacionInfo(
        UUID idPublicacion,
        UUID idAlojamiento,
        BigDecimal precioPorNoche,
        BigDecimal tarifaLimpieza,
        int capacidad
) {
}