package co.edu.unbosque.marketplace.pago.application;

import java.math.BigDecimal;
import java.util.UUID;

public record ReembolsoTrabajo(UUID idReembolso, String paymentIntentId, String stripeRefundId,
        String claveIdempotencia, BigDecimal monto) { }
