package co.edu.unbosque.marketplace.reserva.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConfirmacionReservaResponse(UUID idReserva, String estadoReserva, String estadoPago,
        BigDecimal monto, OffsetDateTime retencionHasta, String referenciaPago, String mensaje) {
}
