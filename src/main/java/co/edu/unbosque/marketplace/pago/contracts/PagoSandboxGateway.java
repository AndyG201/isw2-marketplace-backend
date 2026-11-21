package co.edu.unbosque.marketplace.pago.contracts;

import java.math.BigDecimal;

public interface PagoSandboxGateway {
    ResultadoPagoSandbox procesar(BigDecimal monto, String claveIdempotencia);

    ResultadoReembolsoSandbox reembolsar(
            String paymentIntentId, BigDecimal monto, String claveIdempotencia);

    ResultadoReembolsoSandbox consultarReembolso(String referenciaReembolso);
}
