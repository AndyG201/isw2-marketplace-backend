package co.edu.unbosque.marketplace.pago.infrastructure.gateway;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoReembolsoSandbox;

/**
 * Pasarela de pago que vive solo en memoria. Sirve para correr la aplicación completa (demostraciones, desarrollo,
 * pruebas manuales) sin claves ni conexión a Stripe, igual que {@code CorreoFalso} lo hace con el correo. Se activa con
 * el perfil {@code pago-simulado}; sin ese perfil la aplicación usa {@link PagoSandboxAdapter}.
 *
 * <p>Se comporta como la pasarela real en lo que importa para el resto del sistema:
 * <ul>
 *   <li>Es idempotente: la misma clave devuelve siempre el mismo resultado y no cobra ni reembolsa dos veces.</li>
 *   <li>Valida los mismos datos (monto positivo, clave obligatoria, referencias con el formato de Stripe).</li>
 *   <li>Aprueba todos los pagos, salvo los que tienen exactamente {@code .13} de decimales: ese monto simula una
 *       tarjeta rechazada, para poder probar el camino de rechazo.</li>
 *   <li>Los reembolsos quedan confirmados al instante.</li>
 * </ul>
 * No guarda nada fuera de la memoria: al reiniciar la aplicación se pierde el historial, pero como los pagos y las
 * reservas sí se guardan en la base de datos, lo único que cambia es que una clave repetida volvería a procesarse.
 */
@Component
@Profile("pago-simulado")
public class PagoSimuladoGateway implements PagoSandboxGateway {

    /** Decimales que hacen rechazar un pago, para simular una tarjeta declinada. */
    static final BigDecimal DECIMALES_RECHAZADOS = new BigDecimal("0.13");

    private final Map<String, ResultadoPagoSandbox> pagos = new ConcurrentHashMap<>();
    private final Map<String, ResultadoReembolsoSandbox> reembolsos = new ConcurrentHashMap<>();

    @Override
    public ResultadoPagoSandbox procesar(BigDecimal monto, String claveIdempotencia) {
        if (monto == null || monto.signum() <= 0) {
            throw new DatosInvalidosException("El monto del pago debe ser mayor que cero.");
        }
        exigirClave(claveIdempotencia, "El método de pago y la clave de idempotencia son obligatorios.");
        return pagos.computeIfAbsent(claveIdempotencia, clave -> resolverPago(monto, clave));
    }

    @Override
    public ResultadoReembolsoSandbox reembolsar(String paymentIntentId, BigDecimal monto, String claveIdempotencia) {
        if (paymentIntentId == null || !paymentIntentId.matches("pi_[A-Za-z0-9]+")) {
            throw new DatosInvalidosException("La referencia PaymentIntent no es válida.");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new DatosInvalidosException("El monto y la clave de idempotencia del reembolso son obligatorios.");
        }
        exigirClave(claveIdempotencia, "El monto y la clave de idempotencia del reembolso son obligatorios.");
        return reembolsos.computeIfAbsent(claveIdempotencia, clave -> new ResultadoReembolsoSandbox(
                "re_sim" + sinGuiones(clave),
                "succeeded",
                "Reembolso simulado confirmado."));
    }

    @Override
    public ResultadoReembolsoSandbox consultarReembolso(String referenciaReembolso) {
        if (referenciaReembolso == null || !referenciaReembolso.matches("re_[A-Za-z0-9]+")) {
            throw new DatosInvalidosException("La referencia del reembolso Stripe no es válida.");
        }
        return new ResultadoReembolsoSandbox(referenciaReembolso, "succeeded", "Reembolso simulado confirmado.");
    }

    private ResultadoPagoSandbox resolverPago(BigDecimal monto, String clave) {
        String referencia = "pi_sim" + sinGuiones(clave);
        if (monto.remainder(BigDecimal.ONE).compareTo(DECIMALES_RECHAZADOS) == 0) {
            return new ResultadoPagoSandbox(false, referencia, "Tarjeta rechazada (pago simulado).");
        }
        return new ResultadoPagoSandbox(true, referencia, "Pago simulado aprobado.");
    }

    private void exigirClave(String clave, String mensaje) {
        if (clave == null || clave.isBlank()) {
            throw new DatosInvalidosException(mensaje);
        }
    }

    /** Referencia estable derivada de la clave: la misma clave siempre produce la misma referencia. */
    private String sinGuiones(String clave) {
        return UUID.nameUUIDFromBytes(clave.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
    }
}
