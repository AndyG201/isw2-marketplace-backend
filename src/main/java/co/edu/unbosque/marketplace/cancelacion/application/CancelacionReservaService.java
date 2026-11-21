package co.edu.unbosque.marketplace.cancelacion.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.cancelacion.application.dto.CancelarReservaResponse;
import co.edu.unbosque.marketplace.cancelacion.application.port.CancelacionRepository;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.cancelacion.domain.Cancelacion;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionGateway;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionInfo;

@Service
public class CancelacionReservaService {

    private static final List<String> ESTADOS_CANCELABLES = List.of(
            "PENDIENTE_PAGO", "PENDIENTE_APROBACION", "CONFIRMADA");
    private static final String PAGO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final ReservaCancelacionGateway reservas;
    private final PoliticaCancelacionQuery politicas;
    private final CancelacionRepository cancelaciones;
    private final List<PoliticaDevolucionStrategy> estrategias;
    private AuditoriaEventoCommand auditoria;

    public CancelacionReservaService(
            ReservaCancelacionGateway reservas,
            PoliticaCancelacionQuery politicas,
            CancelacionRepository cancelaciones,
            List<PoliticaDevolucionStrategy> estrategias) {
        this.reservas = reservas;
        this.politicas = politicas;
        this.cancelaciones = cancelaciones;
        this.estrategias = estrategias;
    }

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
    }

    @Transactional
    public CancelarReservaResponse cancelar(UUID idHuesped, UUID idReserva, String motivo) {
        if (idHuesped == null) {
            throw new SesionRequeridaException("Debes iniciar sesión como huésped.");
        }
        if (idReserva == null || motivo == null || motivo.isBlank() || motivo.trim().length() > 1000) {
            throw new DatosInvalidosException("Indica un motivo de cancelación de máximo 1000 caracteres.");
        }

        ReservaCancelacionInfo reserva = reservas.bloquearYConsultar(idReserva)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));
        if (!idHuesped.equals(reserva.idHuesped())) {
            throw new AccesoNoPermitidoException("La reserva no pertenece al huésped autenticado.");
        }

        var cancelacionExistente = cancelaciones.findByIdReserva(idReserva);
        if (cancelacionExistente.isPresent()) {
            return convertir(cancelacionExistente.get(), BigDecimal.ZERO,
                    "La reserva ya había sido cancelada; se devuelve el registro existente.");
        }

        if (!ESTADOS_CANCELABLES.contains(reserva.estado())) {
            throw new ConflictoNegocioException(
                    "La reserva no se puede cancelar en su estado actual: " + reserva.estado() + ".");
        }
        if (PAGO_PENDIENTE.equals(reserva.estadoPago())) {
            throw new ConflictoNegocioException(
                    "La reserva tiene un pago pendiente de confirmación; inténtalo de nuevo cuando finalice el intento.");
        }

        PoliticaCancelacionInfo politica = politicas.consultarPorId(reserva.idPoliticaAplicada())
                .orElseThrow(() -> new ConflictoNegocioException(
                        "No se encontró la política aplicada a la reserva."));
        PoliticaDevolucionStrategy estrategia = estrategias.stream()
                .filter(candidata -> candidata.aplicaA(politica.nombre()))
                .findFirst()
                .orElseThrow(() -> new ConflictoNegocioException(
                        "La política de cancelación no tiene una estrategia de devolución."));

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        long diasAnticipacion = reserva.fechaEntrada() == null
                ? 0
                : ChronoUnit.DAYS.between(LocalDate.now(ZoneOffset.UTC), reserva.fechaEntrada());
        BigDecimal montoPagado = "PAGADO".equals(reserva.estadoPago())
                ? (reserva.montoPagado() == null ? reserva.montoTotal() : reserva.montoPagado())
                : CERO;
        BigDecimal montoDevolucion = estrategia.calcular(montoPagado, politica, diasAnticipacion);
        if (montoDevolucion == null || montoDevolucion.signum() < 0) {
            montoDevolucion = CERO;
        }
        String estadoDevolucion = montoDevolucion.signum() > 0 ? "PROCESADA" : "NO_APLICA";

        Cancelacion cancelacion = new Cancelacion(
                UUID.randomUUID(),
                idReserva,
                idHuesped,
                ahora,
                motivo.trim(),
                montoDevolucion,
                estadoDevolucion,
                ESTADO_CANCELADA);
        cancelaciones.saveAndFlush(cancelacion);
        reservas.cancelarYLiberar(idReserva);
        if (auditoria != null) {
            auditoria.registrarEvento(new RegistrarEventoAuditoria(
                    idHuesped, ahora, "CANCELAR_RESERVA", "EXITOSO", "RESERVA",
                    idReserva,
                    "{\"estadoAnterior\":\"" + reserva.estado()
                            + "\",\"estadoFinal\":\"CANCELADA\"}",
                    null));
        }

        return convertir(cancelacion, politica.porcentajeDevolucion(),
                montoDevolucion.signum() > 0
                        ? "La reserva fue cancelada y la devolución simulada quedó registrada."
                        : "La reserva fue cancelada sin devolución según la política aplicable.");
    }

    private CancelarReservaResponse convertir(Cancelacion cancelacion, BigDecimal porcentaje, String mensaje) {
        return new CancelarReservaResponse(
                cancelacion.getIdCancelacion(),
                cancelacion.getIdReserva(),
                cancelacion.getEstadoFinal(),
                cancelacion.getFechaCancelacion(),
                cancelacion.getMotivo(),
                porcentaje,
                cancelacion.getValorDevolucion(),
                cancelacion.getEstadoDevolucion(),
                mensaje);
    }
}
