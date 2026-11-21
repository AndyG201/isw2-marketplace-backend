package co.edu.unbosque.marketplace.cancelacion.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CancelarReservaResponse(
        UUID idCancelacion,
        UUID idReserva,
        String estadoFinal,
        OffsetDateTime fechaCancelacion,
        String motivo,
        BigDecimal porcentajeDevolucion,
        BigDecimal valorDevolucion,
        String estadoDevolucion,
        String mensaje) {
}
