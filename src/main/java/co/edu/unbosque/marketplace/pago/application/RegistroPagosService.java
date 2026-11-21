package co.edu.unbosque.marketplace.pago.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.pago.application.port.OperacionPendienteRepository;
import co.edu.unbosque.marketplace.pago.application.port.PagoRepository;
import co.edu.unbosque.marketplace.pago.contracts.PagoInfo;
import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;
import co.edu.unbosque.marketplace.pago.domain.OperacionPendiente;
import co.edu.unbosque.marketplace.pago.domain.Pago;

@Service
public class RegistroPagosService implements RegistroPagos {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String EN_PROCESO = "EN_PROCESO";
    private static final String COMPLETADA = "COMPLETADA";
    private static final String FALLIDA = "FALLIDA";
    private static final String PAGADO = "PAGADO";
    private static final String FALLIDO = "FALLIDO";
    private static final int MAXIMO_INTENTOS = 5;

    private final PagoRepository pagos;
    private final OperacionPendienteRepository operaciones;

    public RegistroPagosService(PagoRepository pagos, OperacionPendienteRepository operaciones) {
        this.pagos = pagos;
        this.operaciones = operaciones;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoInfo> buscarPorClave(String claveIdempotencia) {
        return pagos.findByClaveIdempotencia(claveIdempotencia).map(RegistroPagosService::convertir);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagoInfo> ultimoDeReserva(UUID idReserva) {
        return pagos.findFirstByIdReservaOrderByFechaCreacionDesc(idReserva).map(RegistroPagosService::convertir);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> estadoConciliacion(String claveIdempotencia) {
        return operaciones.findByClaveIdempotencia(claveIdempotencia).map(OperacionPendiente::getEstado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tieneConciliacionAbierta(UUID idReserva) {
        return operaciones.findFirstByIdReservaAndEstadoIn(idReserva, List.of(PENDIENTE, EN_PROCESO)).isPresent();
    }

    @Override
    @Transactional
    public PagoInfo iniciarIntento(UUID idReserva, String claveIdempotencia, BigDecimal monto, OffsetDateTime fecha) {
        UUID idPago = UUID.randomUUID();
        Pago pago = pagos.saveAndFlush(new Pago(idPago, idReserva, null, claveIdempotencia, monto,
                PENDIENTE, fecha, null));
        operaciones.saveAndFlush(new OperacionPendiente(
                UUID.randomUUID(), idReserva, idPago, "CONCILIAR_PAGO", PENDIENTE,
                0, fecha, null, null, claveIdempotencia, fecha, fecha));
        return convertir(pago);
    }

    @Override
    @Transactional
    public PagoInfo registrarResultado(
            String claveIdempotencia, boolean aprobado, String referencia, String mensaje, OffsetDateTime fecha) {
        Pago pago = pagos.findByClaveIdempotencia(claveIdempotencia)
                .orElseThrow(() -> new IllegalStateException("No existe el intento de pago."));
        if (referencia != null && !referencia.isBlank()) {
            pago.setReferenciaStripe(referencia);
        }
        pago.setEstado(aprobado ? PAGADO : FALLIDO);
        pago.setFechaPago(aprobado ? fecha : null);
        pagos.saveAndFlush(pago);

        operaciones.findByClaveIdempotencia(claveIdempotencia).ifPresent(operacion -> {
            operacion.setEstado(COMPLETADA);
            operacion.setIntentos(operacion.getIntentos() + 1);
            operacion.setUltimaEjecucion(fecha);
            operacion.setProximaEjecucion(fecha);
            operacion.setUltimoError(aprobado ? null : mensaje);
            operacion.setFechaActualizacion(fecha);
            operaciones.save(operacion);
        });
        return convertir(pago);
    }

    @Override
    @Transactional
    public void registrarFallo(String claveIdempotencia, String error, OffsetDateTime fecha) {
        OperacionPendiente operacion = operaciones.findByClaveIdempotencia(claveIdempotencia)
                .orElseThrow(() -> new IllegalStateException("No existe la operación de conciliación."));
        int intentos = operacion.getIntentos() + 1;
        operacion.setIntentos(intentos);
        operacion.setUltimaEjecucion(fecha);
        operacion.setUltimoError(error == null || error.isBlank()
                ? "Stripe no confirmó el resultado; se reintentará la operación."
                : error);
        if (intentos >= MAXIMO_INTENTOS) {
            operacion.setEstado(FALLIDA);
            operacion.setUltimoError("Se agotaron los reintentos automáticos. " + operacion.getUltimoError());
        } else {
            long segundos = Math.min(300, 15L * (1L << (intentos - 1)));
            operacion.setProximaEjecucion(fecha.plusSeconds(segundos));
        }
        operacion.setFechaActualizacion(fecha);
        operaciones.save(operacion);
    }

    @Override
    @Transactional
    public PagoInfo registrarPago(
            UUID idReserva,
            String referencia,
            String claveIdempotencia,
            BigDecimal monto,
            boolean aprobado,
            OffsetDateTime fecha) {
        Pago pago = new Pago(UUID.randomUUID(), idReserva, referencia, claveIdempotencia, monto,
                aprobado ? PAGADO : FALLIDO, fecha, aprobado ? fecha : null);
        return convertir(pagos.saveAndFlush(pago));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> clavesPorConciliar(OffsetDateTime fecha) {
        return operaciones.findTop25ByEstadoAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(PENDIENTE, fecha)
                .stream()
                .map(OperacionPendiente::getClaveIdempotencia)
                .toList();
    }

    private static PagoInfo convertir(Pago pago) {
        return new PagoInfo(pago.getIdPago(), pago.getIdReserva(), pago.getReferenciaStripe(),
                pago.getClaveIdempotencia(), pago.getMonto(), pago.getEstado(), pago.getFechaCreacion(),
                pago.getFechaPago());
    }
}
