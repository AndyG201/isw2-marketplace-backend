package co.edu.unbosque.marketplace.pago.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoReembolsoSandbox;

@Component
@Profile("worker")
public class ReembolsoPagoWorker {

    private final ReembolsoPagoService servicio;
    private final PagoSandboxGateway stripe;

    public ReembolsoPagoWorker(ReembolsoPagoService servicio, PagoSandboxGateway stripe) {
        this.servicio = servicio;
        this.stripe = stripe;
    }

    @Scheduled(fixedDelayString = "${app.pago.reembolso.intervalo-ms:15000}")
    public void procesarPendientes() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        // reclamarPendientes confirma su transacción antes de iniciar las llamadas de red.
        for (ReembolsoTrabajo trabajo : servicio.reclamarPendientes(ahora)) {
            procesar(trabajo);
        }
    }

    private void procesar(ReembolsoTrabajo trabajo) {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        try {
            ResultadoReembolsoSandbox resultado = trabajo.stripeRefundId() == null
                    ? stripe.reembolsar(trabajo.paymentIntentId(), trabajo.monto(), trabajo.claveIdempotencia())
                    : stripe.consultarReembolso(trabajo.stripeRefundId());
            if (resultado.confirmado() && resultado.referenciaReembolso() != null
                    && !resultado.referenciaReembolso().isBlank()) {
                servicio.confirmar(trabajo.idReembolso(), resultado.referenciaReembolso(), ahora);
            } else if (resultado.confirmado()) {
                servicio.reprogramar(trabajo.idReembolso(), null,
                        "Stripe reportó éxito sin devolver el identificador del reembolso.", ahora);
            } else if (resultado.pendiente()) {
                servicio.reprogramar(trabajo.idReembolso(), resultado.referenciaReembolso(),
                        "Stripe mantiene el reembolso pendiente.", ahora);
            } else {
                servicio.marcarFallaDefinitiva(trabajo.idReembolso(), resultado.referenciaReembolso(),
                        resultado.mensaje(), ahora);
            }
        } catch (RuntimeException error) {
            // Se conserva la misma clave Stripe para que un timeout no genere un segundo reembolso.
            servicio.reprogramar(trabajo.idReembolso(), trabajo.stripeRefundId(), error.getMessage(), ahora);
        }
    }
}
