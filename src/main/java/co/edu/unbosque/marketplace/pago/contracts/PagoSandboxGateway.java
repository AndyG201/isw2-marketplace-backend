package co.edu.unbosque.marketplace.pago.contracts;

import java.math.BigDecimal;

public interface PagoSandboxGateway {
    ResultadoPagoSandbox procesar(BigDecimal monto, String claveIdempotencia);
}
