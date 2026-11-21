package co.edu.unbosque.marketplace.pago.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PagoInfo(
        UUID idPago,
        UUID idReserva,
        String referencia,
        String claveIdempotencia,
        BigDecimal monto,
        String estado,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaPago) {
}
