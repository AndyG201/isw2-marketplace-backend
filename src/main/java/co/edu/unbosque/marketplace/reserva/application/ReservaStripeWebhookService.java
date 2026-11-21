package co.edu.unbosque.marketplace.reserva.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.ExcepcionDominio;
import co.edu.unbosque.marketplace.compartido.domain.ServicioNoDisponibleException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;

@Service
public class ReservaStripeWebhookService {

    private final ObjectMapper mapper;
    private final ReservaPagoIntentoService intentos;
    private final String secretoWebhook;

    public ReservaStripeWebhookService(
            ObjectMapper mapper,
            ReservaPagoIntentoService intentos,
            @Value("${STRIPE_WEBHOOK_SECRET:}") String secretoWebhook) {
        this.mapper = mapper;
        this.intentos = intentos;
        this.secretoWebhook = secretoWebhook == null ? "" : secretoWebhook.trim();
    }

    public void procesar(String cuerpo, String firma) {
        verificarFirma(cuerpo, firma);
        JsonNode evento;
        try {
            evento = mapper.readTree(cuerpo);
        } catch (Exception error) {
            throw new DatosInvalidosException("El evento Stripe no contiene JSON válido.");
        }

        String tipo = evento.path("type").asText("");
        boolean aprobado;
        if ("payment_intent.succeeded".equals(tipo)) {
            aprobado = true;
        } else if ("payment_intent.payment_failed".equals(tipo)
                || "payment_intent.canceled".equals(tipo)) {
            aprobado = false;
        } else {
            return;
        }

        JsonNode objeto = evento.path("data").path("object");
        String referencia = objeto.path("id").asText("");
        String clave = evento.path("request").path("idempotency_key").asText("");
        if (clave.isBlank()) {
            clave = objeto.path("metadata").path("clave_idempotencia").asText("");
        }
        if (clave.isBlank()) {
            return;
        }

        intentos.registrarResultado(
                clave,
                aprobado,
                referencia,
                aprobado ? "Pago confirmado por webhook de Stripe." : "Stripe informó que el pago falló.");
    }

    private void verificarFirma(String cuerpo, String firma) {
        if (secretoWebhook.isBlank()) {
            throw new ServicioNoDisponibleException(
                    "El secreto de firma de Stripe no está configurado.");
        }
        if (cuerpo == null || firma == null || firma.isBlank()) {
            throw new SesionRequeridaException("El webhook no tiene firma válida.");
        }

        String timestamp = null;
        String firmaV1 = null;
        for (String parte : firma.split(",")) {
            String[] valor = parte.trim().split("=", 2);
            if (valor.length == 2 && "t".equals(valor[0])) {
                timestamp = valor[1];
            } else if (valor.length == 2 && "v1".equals(valor[0])) {
                firmaV1 = valor[1];
            }
        }
        if (timestamp == null || firmaV1 == null) {
            throw new SesionRequeridaException("El webhook no tiene firma válida.");
        }

        long epoch;
        try {
            epoch = Long.parseLong(timestamp);
        } catch (NumberFormatException error) {
            throw new SesionRequeridaException("El timestamp de la firma no es válido.");
        }
        if (Duration.between(Instant.ofEpochSecond(epoch), Instant.now()).abs().compareTo(Duration.ofMinutes(5)) > 0) {
            throw new SesionRequeridaException("La firma del webhook expiró.");
        }

        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(secretoWebhook.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] esperado = hmac.doFinal((timestamp + "." + cuerpo).getBytes(StandardCharsets.UTF_8));
            byte[] recibido = HexFormat.of().parseHex(firmaV1);
            if (!MessageDigest.isEqual(esperado, recibido)) {
                throw new SesionRequeridaException("La firma del webhook no coincide.");
            }
        } catch (ExcepcionDominio error) {
            throw error;
        } catch (Exception error) {
            throw new SesionRequeridaException("No se pudo verificar la firma del webhook.");
        }
    }
}
