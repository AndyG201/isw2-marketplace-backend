package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.PlazoVencidoException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.pago.contracts.PagoInfo;
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.application.dto.ConfirmacionReservaResponse;
import co.edu.unbosque.marketplace.reserva.application.port.BloqueoClaveIdempotencia;
import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaConfirmacionDatos;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaConfirmacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.EstadoReserva;
import co.edu.unbosque.marketplace.reserva.contracts.PeriodosHabilitadosQuery;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@Service
public class ReservaConfirmacionService {
    private final ReservaConfirmacionRepository reservas;
    private final RegistroPagos pagos;
    private final NocheReservaRepository noches;
    private final PeriodosHabilitadosQuery periodos;
    private final PagoSandboxGateway pasarela;
    private final BloqueoClaveIdempotencia bloqueo;

    public ReservaConfirmacionService(ReservaConfirmacionRepository reservas, RegistroPagos pagos,
            NocheReservaRepository noches, PeriodosHabilitadosQuery periodos, PagoSandboxGateway pasarela,
            BloqueoClaveIdempotencia bloqueo) {
        this.reservas = reservas;
        this.pagos = pagos;
        this.noches = noches;
        this.periodos = periodos;
        this.pasarela = pasarela;
        this.bloqueo = bloqueo;
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
        if (!periodos.rangoHabilitado(datos.getIdAlojamiento(), datos.getFechaEntrada(), datos.getFechaSalida())
                || !noches.reservarRango(datos.getIdAlojamiento(), idReserva,
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
        bloqueo.bloquear("pago", idempotencia);

        var pagoPrevio = pagos.buscarPorClave(idempotencia);
        if (pagoPrevio.isPresent()) {
            PagoInfo existente = pagoPrevio.get();
            if (!idReserva.equals(existente.idReserva())) {
                throw new ConflictoNegocioException(
                        "La clave de idempotencia ya corresponde a otra operación.");
            }
            return respuesta(reserva, existente, existente.monto(), "Resultado de pago idempotente.");
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
        PagoInfo pago = pagos.registrarPago(idReserva, resultado.referencia(), idempotencia,
                datos.getMontoTotal(), resultado.aprobado(), momento);
        if (resultado.aprobado()) {
            if (reserva.getRetencionHasta() != null && reserva.getRetencionHasta().isAfter(momento)) {
                reserva.cambiarEstado(EstadoReserva.CONFIRMADA);
                reserva.setFechaConfirmacion(momento);
                reserva.setRetencionHasta(null);
                reservas.save(reserva);
                return respuesta(reserva, pago, datos.getMontoTotal(), resultado.mensaje());
            }
            // Stripe capturó el dinero después de vencer la retención: persistir la obligación
            // de reembolso en la misma transacción que registra el pago aprobado.
            pagos.programarReembolso(idempotencia, momento);
            noches.liberar(idReserva);
            reserva.cambiarEstado(EstadoReserva.EXPIRADA);
            reserva.setRetencionHasta(null);
            reservas.save(reserva);
            return respuesta(reserva, pago, datos.getMontoTotal(),
                    "Stripe aprobó el pago después del vencimiento. El reembolso quedó programado.");
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
                if (reserva.estaEn(EstadoReserva.PENDIENTE_PAGO) && reserva.getRetencionHasta() != null
                        && !reserva.getRetencionHasta().isAfter(ahora)) {
                    noches.liberar(id);
                    reserva.cambiarEstado(EstadoReserva.EXPIRADA);
                    reserva.setRetencionHasta(null);
                    reservas.save(reserva);
                }
            });
        }
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
        if (!reserva.estaEn(EstadoReserva.PENDIENTE_PAGO)) {
            throw new ConflictoNegocioException("La reserva no está pendiente de pago.");
        }
    }

    private ReservaConfirmacionDatos datos(Reserva reserva) {
        return reservas.consultarDatos(reserva.getIdCotizacion()).orElseThrow(() ->
                new ConflictoNegocioException("No fue posible recuperar la cotización."));
    }

    private ConfirmacionReservaResponse respuesta(Reserva reserva, PagoInfo pago,
            java.math.BigDecimal monto, String mensaje) {
        return new ConfirmacionReservaResponse(reserva.getIdReserva(), reserva.getEstado(),
                pago == null ? null : pago.estado(), monto, reserva.getRetencionHasta(),
                pago == null ? null : pago.referencia(), mensaje);
    }
}
