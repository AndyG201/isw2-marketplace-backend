package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.pago.domain.Pago;
import co.edu.unbosque.marketplace.pago.infrastructure.persistence.PagoRepository;
import co.edu.unbosque.marketplace.reserva.api.response.ConfirmacionReservaResponse;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.NocheReservaJdbcRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaConfirmacionDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaConfirmacionRepository;

@Service
public class ReservaConfirmacionService {
    private static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private final ReservaConfirmacionRepository reservas;
    private final PagoRepository pagos;
    private final NocheReservaJdbcRepository noches;
    private final PagoSandboxGateway pasarela;
    private final JdbcTemplate jdbc;

    public ReservaConfirmacionService(ReservaConfirmacionRepository reservas, PagoRepository pagos,
            NocheReservaJdbcRepository noches, PagoSandboxGateway pasarela, JdbcTemplate jdbc) {
        this.reservas = reservas;
        this.pagos = pagos;
        this.noches = noches;
        this.pasarela = pasarela;
        this.jdbc = jdbc;
    }

    @Transactional
    public ConfirmacionReservaResponse iniciarRetencion(UUID idUsuario, UUID idReserva) {
        Reserva reserva = bloquearYValidarPropietario(idUsuario, idReserva);
        validarReservaInmediata(reserva);
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (reserva.getRetencionHasta() != null && reserva.getRetencionHasta().isAfter(ahora)) {
            return respuesta(reserva, null, null, "La retención continúa vigente.");
        }
        ReservaConfirmacionDatos datos = datos(reserva);
        if (reserva.getRetencionHasta() != null) {
            noches.liberar(idReserva);
        }
        if (!noches.reservarRango(datos.getIdAlojamiento(), idReserva,
                datos.getFechaEntrada(), datos.getFechaSalida())) {
            throw new ConflictoNegocioException(
                    "El alojamiento dejó de estar disponible para las fechas indicadas.");
        }
        reserva.setRetencionHasta(ahora.plusMinutes(20));
        reservas.save(reserva);
        return respuesta(reserva, null, datos.getMontoTotal(), "Retención de 20 minutos iniciada.");
    }

    @Transactional
    public ConfirmacionReservaResponse confirmar(UUID idUsuario, UUID idReserva, String clave) {
        if (clave == null || clave.isBlank() || clave.trim().length() > 100) {
            throw new DatosInvalidosException(
                    "Debes enviar Idempotency-Key de máximo 100 caracteres.");
        }
        String idempotencia = clave.trim();
        Reserva reserva = bloquearYValidarPropietario(idUsuario, idReserva);
        bloquearClaveIdempotencia(idempotencia);

        var pagoPrevio = pagos.findByClaveIdempotencia(idempotencia);
        if (pagoPrevio.isPresent()) {
            Pago existente = pagoPrevio.get();
            if (!idReserva.equals(existente.getIdReserva())) {
                throw new ConflictoNegocioException(
                        "La clave de idempotencia ya corresponde a otra operación.");
            }
            return respuesta(reserva, existente, existente.getMonto(), "Resultado de pago idempotente.");
        }
        validarReservaInmediata(reserva);
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (reserva.getRetencionHasta() == null || !reserva.getRetencionHasta().isAfter(ahora)) {
            throw new PlazoVencidoException(
                    "La retención expiró; solicita una nueva reserva.");
        }
        ReservaConfirmacionDatos datos = datos(reserva);
        ResultadoPagoSandbox resultado = pasarela.procesar(datos.getMontoTotal(), idempotencia);
        OffsetDateTime momento = OffsetDateTime.now(ZoneOffset.UTC);
        Pago pago = new Pago(UUID.randomUUID(), idReserva, resultado.referencia(), idempotencia,
                datos.getMontoTotal(), resultado.aprobado() ? "PAGADO" : "FALLIDO", momento,
                resultado.aprobado() ? momento : null);
        pagos.saveAndFlush(pago);
        if (resultado.aprobado()) {
            reserva.setEstado("CONFIRMADA");
            reserva.setFechaConfirmacion(momento);
            reserva.setRetencionHasta(null);
            reservas.save(reserva);
            return respuesta(reserva, pago, datos.getMontoTotal(), resultado.mensaje());
        }
        noches.liberar(idReserva);
        reserva.setRetencionHasta(null);
        reservas.save(reserva);
        return respuesta(reserva, pago, datos.getMontoTotal(), resultado.mensaje());
    }

    @Transactional
    public void expirarRetenciones() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        for (UUID id : reservas.buscarRetencionesVencidas(ahora)) {
            reservas.bloquearPorId(id).ifPresent(reserva -> {
                if (PENDIENTE_PAGO.equals(reserva.getEstado()) && reserva.getRetencionHasta() != null
                        && !reserva.getRetencionHasta().isAfter(ahora)) {
                    noches.liberar(id);
                    reserva.setEstado("EXPIRADA");
                    reserva.setRetencionHasta(null);
                    reservas.save(reserva);
                }
            });
        }
    }

    private void bloquearClaveIdempotencia(String clave) {
        jdbc.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                resultado -> {
                    resultado.next();
                    return null;
                },
                "pago:" + clave);
    }

    private Reserva bloquearYValidarPropietario(UUID usuario, UUID idReserva) {
        if (usuario == null) {
            throw new SesionRequeridaException("Debes iniciar sesión.");
        }
        Reserva reserva = reservas.bloquearPorId(idReserva).orElseThrow(() ->
                new RecursoNoEncontradoException("La reserva no existe."));
        if (!usuario.equals(reserva.getIdHuesped())) {
            throw new AccesoNoPermitidoException("La reserva pertenece a otro usuario.");
        }
        return reserva;
    }

    private void validarReservaInmediata(Reserva reserva) {
        if (!"INMEDIATA".equals(reserva.getModoAceptacion())) {
            throw new ConflictoNegocioException(
                    "La confirmación inmediata solo aplica a reservas de aceptación inmediata.");
        }
        if (!PENDIENTE_PAGO.equals(reserva.getEstado())) {
            throw new ConflictoNegocioException("La reserva no está pendiente de pago.");
        }
    }

    private ReservaConfirmacionDatos datos(Reserva reserva) {
        return reservas.consultarDatos(reserva.getIdCotizacion()).orElseThrow(() ->
                new ConflictoNegocioException("No fue posible recuperar la cotización."));
    }

    private ConfirmacionReservaResponse respuesta(Reserva reserva, Pago pago,
            java.math.BigDecimal monto, String mensaje) {
        return new ConfirmacionReservaResponse(reserva.getIdReserva(), reserva.getEstado(),
                pago == null ? null : pago.getEstado(), monto, reserva.getRetencionHasta(),
                pago == null ? null : pago.getReferenciaStripe(), mensaje);
    }
}
