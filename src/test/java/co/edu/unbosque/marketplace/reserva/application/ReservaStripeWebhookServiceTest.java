package co.edu.unbosque.marketplace.reserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.ServicioNoDisponibleException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;

class ReservaStripeWebhookServiceTest {

    private static final String SECRETO = "whsec_prueba";
    private static final String EVENTO = """
            {"type":"payment_intent.succeeded","data":{"object":{"id":"pi_123",\
            "metadata":{"clave_idempotencia":"pago-1"}}}}""";

    private ReservaPagoIntentoService intentos;
    private ReservaStripeWebhookService servicio;

    @BeforeEach
    void preparar() {
        intentos = mock(ReservaPagoIntentoService.class);
        servicio = new ReservaStripeWebhookService(new ObjectMapper(), intentos, SECRETO);
    }

    @Test
    void registraElPagoAprobadoCuandoLaFirmaEsValida() throws Exception {
        servicio.procesar(EVENTO, firma(EVENTO, Instant.now().getEpochSecond()));

        verify(intentos).registrarResultado(eq("pago-1"), eq(true), eq("pi_123"), any());
    }

    @Test
    void sinSecretoConfiguradoElWebhookNoEstaDisponible() {
        ReservaStripeWebhookService sinSecreto = new ReservaStripeWebhookService(new ObjectMapper(), intentos, " ");

        ServicioNoDisponibleException error = assertThrows(ServicioNoDisponibleException.class,
                () -> sinSecreto.procesar(EVENTO, "t=1,v1=abc"));
        assertEquals("El secreto de firma de Stripe no está configurado.", error.getMessage());
    }

    @Test
    void rechazaUnEventoSinFirma() {
        assertRechazado(EVENTO, null, "El webhook no tiene firma válida.");
    }

    @Test
    void rechazaUnaFirmaSinTimestampNiV1() {
        assertRechazado(EVENTO, "otra=valor", "El webhook no tiene firma válida.");
    }

    @Test
    void rechazaUnTimestampQueNoEsNumero() {
        assertRechazado(EVENTO, "t=ayer,v1=abc", "El timestamp de la firma no es válido.");
    }

    @Test
    void rechazaUnaFirmaVencida() throws Exception {
        long haceUnaHora = Instant.now().minusSeconds(3600).getEpochSecond();
        assertRechazado(EVENTO, firma(EVENTO, haceUnaHora), "La firma del webhook expiró.");
    }

    @Test
    void rechazaUnaFirmaQueNoCoincide() {
        long ahora = Instant.now().getEpochSecond();
        assertRechazado(EVENTO, "t=" + ahora + ",v1=" + "00".repeat(32), "La firma del webhook no coincide.");
    }

    @Test
    void rechazaUnaFirmaQueNoEsHexadecimal() {
        long ahora = Instant.now().getEpochSecond();
        assertRechazado(EVENTO, "t=" + ahora + ",v1=zz", "No se pudo verificar la firma del webhook.");
    }

    @Test
    void rechazaUnCuerpoQueNoEsJson() throws Exception {
        String cuerpo = "no es json";

        assertThrows(DatosInvalidosException.class,
                () -> servicio.procesar(cuerpo, firma(cuerpo, Instant.now().getEpochSecond())));
        verify(intentos, never()).registrarResultado(any(), anyBoolean(), any(), any());
    }

    private void assertRechazado(String cuerpo, String firma, String mensaje) {
        SesionRequeridaException error = assertThrows(SesionRequeridaException.class,
                () -> servicio.procesar(cuerpo, firma));
        assertEquals(mensaje, error.getMessage());
        verify(intentos, never()).registrarResultado(any(), anyBoolean(), any(), any());
    }

    private static String firma(String cuerpo, long timestamp) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(SECRETO.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] valor = hmac.doFinal((timestamp + "." + cuerpo).getBytes(StandardCharsets.UTF_8));
        return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(valor);
    }
}
