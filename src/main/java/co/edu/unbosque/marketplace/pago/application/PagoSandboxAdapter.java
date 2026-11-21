package co.edu.unbosque.marketplace.pago.application;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoReembolsoSandbox;

@Component
public class PagoSandboxAdapter implements PagoSandboxGateway {
    private static final URI PAYMENT_INTENTS_URI = URI.create("https://api.stripe.com/v1/payment_intents");
    private static final URI REFUNDS_URI = URI.create("https://api.stripe.com/v1/refunds");
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;
    private final String currency;
    private final String paymentMethod;

    public PagoSandboxAdapter(
            ObjectMapper objectMapper,
            @Value("${STRIPE_SECRET_KEY:}") String secretKey,
            @Value("${app.pago.stripe.currency:cop}") String currency,
            @Value("${app.pago.stripe.payment-method:pm_card_visa}") String paymentMethod) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.objectMapper = objectMapper;
        this.secretKey = secretKey == null ? "" : secretKey.trim();
        this.currency = currency == null ? "cop" : currency.trim().toLowerCase();
        this.paymentMethod = paymentMethod == null ? "pm_card_visa" : paymentMethod.trim();
    }

    @Override
    public ResultadoPagoSandbox procesar(BigDecimal monto, String claveIdempotencia) {
        validarConfiguracion(monto, claveIdempotencia);
        long montoEnUnidadMenor;
        try {
            // Stripe recibe COP en centavos (dos decimales), igual que otras monedas de dos decimales.
            montoEnUnidadMenor = monto.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("El monto no se puede representar en la moneda configurada.", ex);
        }

        String body = "amount=" + montoEnUnidadMenor
                + "&currency=" + codificar(currency)
                + "&payment_method=" + codificar(paymentMethod)
                + "&confirm=true&off_session=true";
        String basicAuth = Base64.getEncoder().encodeToString(
                (secretKey + ":").getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(PAYMENT_INTENTS_URI)
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Idempotency-Key", claveIdempotencia)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode json = objectMapper.readTree(response.body());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String id = texto(json, "id");
                String status = texto(json, "status");
                if ("succeeded".equals(status)) {
                    return new ResultadoPagoSandbox(true, id, "Pago de prueba aprobado por Stripe.");
                }
                return new ResultadoPagoSandbox(false, id,
                        "Stripe no confirmó el pago. Estado recibido: " + status + ".");
            }
            String mensaje = json.path("error").path("message").asText("Stripe rechazó la solicitud de pago.");
            if (response.statusCode() == 402) {
                String paymentIntentId = json.path("error").path("payment_intent").path("id").asText(null);
                return new ResultadoPagoSandbox(false, paymentIntentId, mensaje);
            }
            throw new IllegalStateException("Stripe respondió HTTP " + response.statusCode() + ": " + mensaje);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La solicitud a Stripe fue interrumpida.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible comunicarse con Stripe.", ex);
        }
    }


    @Override
    public ResultadoReembolsoSandbox reembolsar(
            String paymentIntentId, BigDecimal monto, String claveIdempotencia) {
        validarConfiguracionReembolso(paymentIntentId, monto, claveIdempotencia);
        long montoMenor = monto.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        String body = "payment_intent=" + codificar(paymentIntentId)
                + "&amount=" + montoMenor;
        HttpRequest request = HttpRequest.newBuilder(REFUNDS_URI)
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", autorizacion())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Idempotency-Key", claveIdempotencia)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return enviarReembolso(request);
    }

    @Override
    public ResultadoReembolsoSandbox consultarReembolso(String referenciaReembolso) {
        if (referenciaReembolso == null || !referenciaReembolso.matches("re_[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("La referencia del reembolso Stripe no es válida.");
        }
        URI uri = URI.create(REFUNDS_URI + "/" + codificar(referenciaReembolso));
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", autorizacion())
                .GET()
                .build();
        return enviarReembolso(request);
    }

    private ResultadoReembolsoSandbox enviarReembolso(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode json = objectMapper.readTree(response.body());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new ResultadoReembolsoSandbox(
                        texto(json, "id"), texto(json, "status"),
                        json.path("failure_reason").asText("Stripe procesó la solicitud de reembolso."));
            }
            String mensaje = json.path("error").path("message")
                    .asText("Stripe rechazó la solicitud de reembolso.");
            throw new IllegalStateException("Stripe respondió HTTP " + response.statusCode() + ": " + mensaje);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La solicitud de reembolso a Stripe fue interrumpida.", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible consultar o solicitar el reembolso en Stripe.", ex);
        }
    }

    private void validarConfiguracionReembolso(String paymentIntentId, BigDecimal monto, String clave) {
        if (!secretKey.startsWith("sk_test_")) {
            throw new IllegalStateException("Configura STRIPE_SECRET_KEY con una clave de prueba sk_test_.");
        }
        if (paymentIntentId == null || !paymentIntentId.matches("pi_[A-Za-z0-9]+")) {
            throw new IllegalArgumentException("La referencia PaymentIntent no es válida.");
        }
        if (monto == null || monto.signum() <= 0 || clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("El monto y la clave de idempotencia del reembolso son obligatorios.");
        }
    }

    private String autorizacion() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }

    private void validarConfiguracion(BigDecimal monto, String claveIdempotencia) {
        if (!secretKey.startsWith("sk_test_")) {
            throw new IllegalStateException(
                    "Configura STRIPE_SECRET_KEY con una clave secreta de prueba que comience por sk_test_.");
        }
        if (currency.isBlank() || !currency.matches("[a-z]{3}")) {
            throw new IllegalStateException("La moneda configurada para Stripe no es válida.");
        }
        if (paymentMethod.isBlank() || claveIdempotencia == null || claveIdempotencia.isBlank()) {
            throw new IllegalArgumentException("El método de pago y la clave de idempotencia son obligatorios.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor que cero.");
        }
    }

    private String texto(JsonNode json, String campo) {
        return json.path(campo).asText("");
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
