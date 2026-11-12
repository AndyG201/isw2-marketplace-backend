package co.edu.unbosque.marketplace.cancelacion.contracts;

import java.math.BigDecimal;
import java.util.UUID;

public record PoliticaCancelacionInfo(
        UUID idPolitica,
        String nombre,
        int version,
        String descripcion,
        Integer anticipacionHoras,
        BigDecimal porcentajeDevolucion,
        boolean activa
) {
}
