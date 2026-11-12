package co.edu.unbosque.marketplace.servicio.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicioAdicionalInfo(
        UUID idAdicional,
        UUID idPublicacion,
        String nombre,
        String descripcion,
        BigDecimal costo,
        boolean activo
) {
}
