package co.edu.unbosque.marketplace.reserva.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.marketplace.reserva.application.ReservaStripeWebhookService;

@RestController
@RequestMapping("/reservas/webhooks/stripe")
public class ReservaStripeWebhookController {

    private final ReservaStripeWebhookService servicio;

    public ReservaStripeWebhookController(ReservaStripeWebhookService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<Void> recibir(
            @RequestBody String cuerpo,
            @RequestHeader(name = "Stripe-Signature", required = false) String firma) {
        servicio.procesar(cuerpo, firma);
        return ResponseEntity.ok().build();
    }
}
