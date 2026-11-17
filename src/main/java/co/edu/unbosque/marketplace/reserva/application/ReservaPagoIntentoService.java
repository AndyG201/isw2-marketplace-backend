package co.edu.unbosque.marketplace.reserva.application;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.pago.domain.OperacionPendiente;
import co.edu.unbosque.marketplace.pago.domain.Pago;
import co.edu.unbosque.marketplace.pago.infrastructure.persistence.OperacionPendienteRepository;
import co.edu.unbosque.marketplace.pago.infrastructure.persistence.PagoRepository;
import co.edu.unbosque.marketplace.reserva.api.response.PagoSimuladoResponse;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaConfirmacionDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaConfirmacionRepository;

@Service
public class ReservaPagoIntentoService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String EN_PROCESO = "EN_PROCESO";
    private static final String COMPLETADA = "COMPLETADA";
    private static final String FALLIDA = "FALLIDA";
    private static final String PAGO_PENDIENTE = "PENDIENTE";
    private static final String PAGO_PAGADO = "PAGADO";
    private static final String PAGO_FALLIDO = "FALLIDO";
    private static final String RESERVA_PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private static final String RESERVA_CONFIRMADA = "CONFIRMADA";

    private final ReservaConfirmacionRepository reservas;
    private final PagoRepository pagos;
    private final OperacionPendienteRepository operaciones;
    private final JdbcTemplate jdbc;

    public ReservaPagoIntentoService(
            ReservaConfirmacionRepository reservas,
            PagoRepository pagos,
            OperacionPendienteRepository operaciones,
            JdbcTemplate jdbc) {
        this.reservas = reservas;
        this.pagos = pagos;
        this.operaciones = operaciones;
        this.jdbc = jdbc;
    }

    @Transactional
    public PagoIntentoPreparado preparar(UUID idHuesped, UUID idReserva, String clave) {
        if (idHuesped == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión.");
        }
        if (idReserva == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador de la reserva es obligatorio.");
        }
        String claveNormalizada = normalizarClave(clave);
        bloquearClave(claveNormalizada);

        Reserva reserva = reservas.bloquearPorId(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe."));
        if (!idHuesped.equals(reserva.getIdHuesped())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La reserva pertenece a otro huésped.");
        }

        Optional<Pago> pagoConClave = pagos.findByClaveIdempotencia(claveNormalizada);
        if (pagoConClave.isPresent()) {
            Pago existente = pagoConClave.get();
            if (!idReserva.equals(existente.getIdReserva())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La clave de idempotencia ya corresponde a otra reserva.");
            }
            return prepararExistente(reserva, existente);
        }

        Optional<Pago> ultimoPago = pagos.findFirstByIdReservaOrderByFechaCreacionDesc(idReserva);
        if (ultimoPago.isPresent() && PAGO_PAGADO.equals(ultimoPago.get().getEstado())) {
            return convertir(reserva, ultimoPago.get(), "La reserva ya cuenta con un pago aprobado.", false);
        }
        if (ultimoPago.isPresent() && PAGO_PENDIENTE.equals(ultimoPago.get().getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La reserva tiene un pago pendiente de conciliación. Reintenta con la misma clave de idempotencia.");
        }

        if (!RESERVA_PENDIENTE_PAGO.equals(reserva.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La reserva no está pendiente de pago.");
        }
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (reserva.getRetencionHasta() == null || !reserva.getRetencionHasta().isAfter(ahora)) {
            throw new ResponseStatusException(HttpStatus.GONE, "El plazo de pago de la reserva venció.");
        }

        operaciones.findFirstByIdReservaAndEstadoIn(idReserva, List.of(PENDIENTE, EN_PROCESO))
                .ifPresent(operacion -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Ya existe un intento de pago pendiente para la reserva.");
                });

        ReservaConfirmacionDatos datos = reservas.consultarDatos(reserva.getIdCotizacion())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No fue posible recuperar el monto de la reserva."));
        BigDecimal monto = datos.getMontoTotal();
        if (monto == null || monto.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El monto calculado para la reserva no es válido.");
        }

        UUID idPago = UUID.randomUUID();
        Pago pago = new Pago(idPago, idReserva, null, claveNormalizada, monto,
                PAGO_PENDIENTE, ahora, null);
        pagos.saveAndFlush(pago);

        OperacionPendiente operacion = new OperacionPendiente(
                UUID.randomUUID(), idReserva, idPago, "CONCILIAR_PAGO", PENDIENTE,
                0, ahora, null, null, claveNormalizada, ahora, ahora);
        operaciones.saveAndFlush(operacion);

        return new PagoIntentoPreparado(
                idReserva, idPago, claveNormalizada, monto, reserva.getEstado(), PAGO_PENDIENTE,
                reserva.getRetencionHasta(), null, "Procesando el pago de prueba en Stripe.", true);
    }

    @Transactional(readOnly = true)
    public PagoIntentoPreparado buscarPendiente(String clave) {
        String claveNormalizada = normalizarClave(clave);
        Pago pago = pagos.findByClaveIdempotencia(claveNormalizada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el intento de pago."));
        Reserva reserva = reservas.findById(pago.getIdReserva())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe."));
        OperacionPendiente operacion = operaciones.findByClaveIdempotencia(claveNormalizada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No existe la operación de conciliación."));
        if (!PENDIENTE.equals(operacion.getEstado())) {
            return convertir(reserva, pago, "El intento de pago ya no está pendiente.", false);
        }
        return convertir(reserva, pago, "Reintentando el pago de prueba.", true);
    }

    @Transactional
    public PagoSimuladoResponse registrarResultado(
            String clave,
            boolean aprobado,
            String referencia,
            String mensaje) {
        String claveNormalizada = normalizarClave(clave);
        Pago pago = pagos.findByClaveIdempotencia(claveNormalizada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el intento de pago."));
        Reserva reserva = reservas.bloquearPorId(pago.getIdReserva())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe."));
        OperacionPendiente operacion = operaciones.findByClaveIdempotencia(claveNormalizada).orElse(null);

        if (PAGO_PAGADO.equals(pago.getEstado())) {
            return respuesta(reserva, pago, "El pago ya había sido aprobado.");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (referencia != null && !referencia.isBlank()) {
            pago.setReferenciaStripe(referencia);
        }
        pago.setEstado(aprobado ? PAGO_PAGADO : PAGO_FALLIDO);
        pago.setFechaPago(aprobado ? ahora : null);
        pagos.saveAndFlush(pago);

        if (operacion != null) {
            operacion.setEstado(COMPLETADA);
            operacion.setIntentos(operacion.getIntentos() + 1);
            operacion.setUltimaEjecucion(ahora);
            operacion.setProximaEjecucion(ahora);
            operacion.setUltimoError(aprobado ? null : mensaje);
            operacion.setFechaActualizacion(ahora);
            operaciones.save(operacion);
        }

        String mensajeFinal = mensaje;
        if (aprobado && RESERVA_PENDIENTE_PAGO.equals(reserva.getEstado())) {
            if (reserva.getRetencionHasta() != null && reserva.getRetencionHasta().isAfter(ahora)) {
                reserva.setEstado(RESERVA_CONFIRMADA);
                reserva.setFechaConfirmacion(ahora);
                reserva.setRetencionHasta(null);
                reservas.save(reserva);
            } else {
                mensajeFinal = "Stripe aprobó el pago, pero venció la retención de la reserva; requiere conciliación.";
            }
        }
        return respuesta(reserva, pago, mensajeFinal);
    }

    @Transactional
    public PagoSimuladoResponse registrarError(String clave, String error) {
        String claveNormalizada = normalizarClave(clave);
        Pago pago = pagos.findByClaveIdempotencia(claveNormalizada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el intento de pago."));
        Reserva reserva = reservas.findById(pago.getIdReserva())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe."));
        OperacionPendiente operacion = operaciones.findByClaveIdempotencia(claveNormalizada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No existe la operación de conciliación."));
        if (!PENDIENTE.equals(operacion.getEstado())) {
            return respuesta(reserva, pago, "El resultado del pago ya fue registrado.");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        int intentos = operacion.getIntentos() + 1;
        operacion.setIntentos(intentos);
        operacion.setUltimaEjecucion(ahora);
        operacion.setUltimoError(error == null || error.isBlank()
                ? "Stripe no confirmó el resultado; se reintentará la operación."
                : error);
        if (intentos >= 5) {
            operacion.setEstado(FALLIDA);
            operacion.setUltimoError("Se agotaron los reintentos automáticos. " + operacion.getUltimoError());
        } else {
            long segundos = Math.min(300, 15L * (1L << (intentos - 1)));
            operacion.setProximaEjecucion(ahora.plusSeconds(segundos));
        }
        operacion.setFechaActualizacion(ahora);
        operaciones.save(operacion);
        return respuesta(reserva, pago, "El pago no tiene aún un resultado confirmado; se conciliará automáticamente.");
    }

    private PagoIntentoPreparado prepararExistente(Reserva reserva, Pago pago) {
        boolean pendiente = PAGO_PENDIENTE.equals(pago.getEstado())
                && operaciones.findByClaveIdempotencia(pago.getClaveIdempotencia())
                        .map(operacion -> PENDIENTE.equals(operacion.getEstado()))
                        .orElse(false);
        String mensaje = pendiente
                ? "El pago sigue pendiente; se consultará con la misma clave de idempotencia."
                : "Se devuelve el resultado del intento de pago existente.";
        return convertir(reserva, pago, mensaje, pendiente);
    }

    private PagoIntentoPreparado convertir(Reserva reserva, Pago pago, String mensaje, boolean procesar) {
        return new PagoIntentoPreparado(
                reserva.getIdReserva(), pago.getIdPago(), pago.getClaveIdempotencia(), pago.getMonto(),
                reserva.getEstado(), pago.getEstado(), reserva.getRetencionHasta(), pago.getReferenciaStripe(),
                mensaje, procesar);
    }

    private PagoSimuladoResponse respuesta(Reserva reserva, Pago pago, String mensaje) {
        return new PagoSimuladoResponse(
                reserva.getIdReserva(), pago.getIdPago(), reserva.getEstado(), pago.getEstado(), pago.getMonto(),
                reserva.getRetencionHasta(), pago.getReferenciaStripe(), mensaje);
    }

    private String normalizarClave(String clave) {
        if (clave == null || clave.isBlank() || clave.trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes enviar Idempotency-Key de máximo 100 caracteres.");
        }
        return clave.trim();
    }

    private void bloquearClave(String clave) {
        jdbc.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                resultado -> {
                    resultado.next();
                    return null;
                },
                "pago:" + clave);
    }
}
