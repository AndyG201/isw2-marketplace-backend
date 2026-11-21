package co.edu.unbosque.marketplace.pago.contracts;

/** Resultado normalizado de una operación de reembolso en Stripe. */
public record ResultadoReembolsoSandbox(
        String referenciaReembolso,
        String estado,
        String mensaje) {

    public boolean confirmado() {
        return "succeeded".equalsIgnoreCase(estado);
    }

    public boolean pendiente() {
        return "pending".equalsIgnoreCase(estado) || "requires_action".equalsIgnoreCase(estado);
    }

    public boolean fallido() {
        return "failed".equalsIgnoreCase(estado) || "canceled".equalsIgnoreCase(estado);
    }
}
