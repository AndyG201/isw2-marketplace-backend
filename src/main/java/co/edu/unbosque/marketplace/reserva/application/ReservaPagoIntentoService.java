package co.edu.unbosque.marketplace.reserva.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.PlazoVencidoException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.pago.contracts.PagoInfo;
import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;
import co.edu.unbosque.marketplace.reserva.application.dto.PagoSimuladoResponse;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaConfirmacionDatos;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaConfirmacionRepository;
import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@Service
public class ReservaPagoIntentoService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String PAGO_PENDIENTE = "PENDIENTE";
    private static final String PAGO_PAGADO = "PAGADO";
    private static final String PAGO_REEMBOLSADO = "REEMBOLSADO";
    private static final String RESERVA_PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private static final String RESERVA_CONFIRMADA = "CONFIRMADA";

    private final ReservaConfirmacionRepository reservas;
    private final RegistroPagos pagos;
    private final JdbcTemplate jdbc;
    private final NocheReservaRepository noches;

    public ReservaPagoIntentoService(
            ReservaConfirmacionRepository reservas,
            RegistroPagos pagos,
            JdbcTemplate jdbc,
            NocheReservaRepository noches) {
        this.reservas = reservas;
        this.pagos = pagos;
        this.jdbc = jdbc;
        this.noches = noches;
    }

    @Transactional
    public PagoIntentoPreparado preparar(UUID idHuesped, UUID idReserva, String clave) {
        if (idHuesped == null) {
            throw new SesionRequeridaException("Debes iniciar sesión.");
        }
        if (idReserva == null) {
            throw new DatosInvalidosException("El identificador de la reserva es obligatorio.");
        }
        String claveNormalizada = normalizarClave(clave);
        bloquearClave(claveNormalizada);

        Reserva reserva = reservas.bloquearPorId(idReserva)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));
        if (!idHuesped.equals(reserva.getIdHuesped())) {
            throw new AccesoNoPermitidoException("La reserva pertenece a otro huésped.");
        }

        Optional<PagoInfo> pagoConClave = pagos.buscarPorClave(claveNormalizada);
        if (pagoConClave.isPresent()) {
            PagoInfo existente = pagoConClave.get();
            if (!idReserva.equals(existente.idReserva())) {
                throw new ConflictoNegocioException(
                        "La clave de idempotencia ya corresponde a otra reserva.");
            }
            return prepararExistente(reserva, existente);
        }

        Optional<PagoInfo> ultimoPago = pagos.ultimoDeReserva(idReserva);
        if (ultimoPago.isPresent() && PAGO_PAGADO.equals(ultimoPago.get().estado())) {
            return convertir(reserva, ultimoPago.get(), "La reserva ya cuenta con un pago aprobado.", false);
        }
        if (ultimoPago.isPresent() && PAGO_PENDIENTE.equals(ultimoPago.get().estado())) {
            throw new ConflictoNegocioException(
                    "La reserva tiene un pago pendiente de conciliación. Reintenta con la misma clave de idempotencia.");
        }

        if (!RESERVA_PENDIENTE_PAGO.equals(reserva.getEstado())) {
            throw new ConflictoNegocioException("La reserva no está pendiente de pago.");
        }
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (reserva.getRetencionHasta() == null || !reserva.getRetencionHasta().isAfter(ahora)) {
            throw new PlazoVencidoException("El plazo de pago de la reserva venció.");
        }

        if (pagos.tieneConciliacionAbierta(idReserva)) {
            throw new ConflictoNegocioException(
                    "Ya existe un intento de pago pendiente para la reserva.");
        }

        ReservaConfirmacionDatos datos = reservas.consultarDatos(reserva.getIdCotizacion())
                .orElseThrow(() -> new ConflictoNegocioException(
                        "No fue posible recuperar el monto de la reserva."));
        BigDecimal monto = datos.getMontoTotal();
        if (monto == null || monto.signum() <= 0) {
            throw new ConflictoNegocioException("El monto calculado para la reserva no es válido.");
        }

        PagoInfo pago = pagos.iniciarIntento(idReserva, claveNormalizada, monto, ahora);

        return new PagoIntentoPreparado(
                idReserva, pago.idPago(), claveNormalizada, monto, reserva.getEstado(), PAGO_PENDIENTE,
                reserva.getRetencionHasta(), null, "Procesando el pago de prueba en Stripe.", true);
    }

    @Transactional(readOnly = true)
    public PagoIntentoPreparado buscarPendiente(String clave) {
        String claveNormalizada = normalizarClave(clave);
        PagoInfo pago = pagos.buscarPorClave(claveNormalizada)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el intento de pago."));
        Reserva reserva = reservas.findById(pago.idReserva())
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));
        String estadoConciliacion = pagos.estadoConciliacion(claveNormalizada)
                .orElseThrow(() -> new ConflictoNegocioException("No existe la operación de conciliación."));
        if (!PENDIENTE.equals(estadoConciliacion)) {
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
        PagoInfo registrado = pagos.buscarPorClave(claveNormalizada)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el intento de pago."));
        Reserva reserva = reservas.bloquearPorId(registrado.idReserva())
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));

        if (PAGO_PAGADO.equals(registrado.estado())) {
            return respuesta(reserva, registrado, "El pago ya había sido aprobado.");
        }
        if (PAGO_REEMBOLSADO.equals(registrado.estado())) {
            return respuesta(reserva, registrado, "El pago ya fue reembolsado; se ignora el evento duplicado.");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        PagoInfo pago = pagos.registrarResultado(claveNormalizada, aprobado, referencia, mensaje, ahora);

        String mensajeFinal = mensaje;
        if (aprobado) {
            boolean reservaPagable = RESERVA_PENDIENTE_PAGO.equals(reserva.getEstado())
                    && reserva.getRetencionHasta() != null
                    && reserva.getRetencionHasta().isAfter(ahora);
            if (reservaPagable) {
                reserva.setEstado(RESERVA_CONFIRMADA);
                reserva.setFechaConfirmacion(ahora);
                reserva.setRetencionHasta(null);
                reservas.save(reserva);
            } else {
                // Webhook/reintento tardío: el pago está capturado, pero la reserva ya no
                // puede confirmarse. La obligación de reembolso queda persistida aquí.
                pagos.programarReembolso(claveNormalizada, ahora);
                if (RESERVA_PENDIENTE_PAGO.equals(reserva.getEstado())) {
                    noches.liberar(reserva.getIdReserva());
                    reserva.setEstado("EXPIRADA");
                    reserva.setRetencionHasta(null);
                    reservas.save(reserva);
                }
                mensajeFinal = "Stripe aprobó el pago después del vencimiento. El reembolso quedó programado.";
            }
        }
        return respuesta(reserva, pago, mensajeFinal);
    }

    @Transactional
    public PagoSimuladoResponse registrarError(String clave, String error) {
        String claveNormalizada = normalizarClave(clave);
        PagoInfo pago = pagos.buscarPorClave(claveNormalizada)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el intento de pago."));
        Reserva reserva = reservas.findById(pago.idReserva())
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));
        String estadoConciliacion = pagos.estadoConciliacion(claveNormalizada)
                .orElseThrow(() -> new ConflictoNegocioException("No existe la operación de conciliación."));
        if (!PENDIENTE.equals(estadoConciliacion)) {
            return respuesta(reserva, pago, "El resultado del pago ya fue registrado.");
        }

        pagos.registrarFallo(claveNormalizada, error, OffsetDateTime.now(ZoneOffset.UTC));
        return respuesta(reserva, pago, "El pago no tiene aún un resultado confirmado; se conciliará automáticamente.");
    }

    private PagoIntentoPreparado prepararExistente(Reserva reserva, PagoInfo pago) {
        boolean pendiente = PAGO_PENDIENTE.equals(pago.estado())
                && pagos.estadoConciliacion(pago.claveIdempotencia())
                        .map(PENDIENTE::equals)
                        .orElse(false);
        String mensaje = pendiente
                ? "El pago sigue pendiente; se consultará con la misma clave de idempotencia."
                : "Se devuelve el resultado del intento de pago existente.";
        return convertir(reserva, pago, mensaje, pendiente);
    }

    private PagoIntentoPreparado convertir(Reserva reserva, PagoInfo pago, String mensaje, boolean procesar) {
        return new PagoIntentoPreparado(
                reserva.getIdReserva(), pago.idPago(), pago.claveIdempotencia(), pago.monto(),
                reserva.getEstado(), pago.estado(), reserva.getRetencionHasta(), pago.referencia(),
                mensaje, procesar);
    }

    private PagoSimuladoResponse respuesta(Reserva reserva, PagoInfo pago, String mensaje) {
        return new PagoSimuladoResponse(
                reserva.getIdReserva(), pago.idPago(), reserva.getEstado(), pago.estado(), pago.monto(),
                reserva.getRetencionHasta(), pago.referencia(), mensaje);
    }

    private String normalizarClave(String clave) {
        if (clave == null || clave.isBlank() || clave.trim().length() > 100) {
            throw new DatosInvalidosException(
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
