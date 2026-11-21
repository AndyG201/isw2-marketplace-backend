package co.edu.unbosque.marketplace.reserva.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PagoIntentoPreparado(
        UUID idReserva,
        UUID idPago,
        String claveIdempotencia,
        BigDecimal monto,
        String estadoReserva,
        String estadoPago,
        OffsetDateTime retencionHasta,
        String referenciaPago,
        String mensaje,
        boolean requiereProcesamiento) {
}
