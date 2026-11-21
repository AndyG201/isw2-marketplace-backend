package co.edu.unbosque.marketplace.pago.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.pago.application.port.PagoRepository;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsoPagoRepository;
import co.edu.unbosque.marketplace.pago.domain.Pago;
import co.edu.unbosque.marketplace.pago.domain.ReembolsoPago;

@Service
public class ReembolsoPagoService {

    private static final int MAXIMO_INTENTOS = 12;
    private final JdbcTemplate jdbc;
    private final ReembolsoPagoRepository reembolsos;
    private final PagoRepository pagos;

    public ReembolsoPagoService(JdbcTemplate jdbc, ReembolsoPagoRepository reembolsos, PagoRepository pagos) {
        this.jdbc = jdbc;
        this.reembolsos = reembolsos;
        this.pagos = pagos;
    }

    /** Reclama trabajo en una transacción corta; el llamado HTTP a Stripe ocurre después del commit. */
    @Transactional
    public List<ReembolsoTrabajo> reclamarPendientes(OffsetDateTime ahora) {
        String sql = """
                WITH candidatas AS (
                    SELECT r.id_reembolso
                    FROM public.reembolso_pago r
                    WHERE (r.estado = 'PENDIENTE' AND r.proxima_ejecucion <= ?)
                       OR (r.estado = 'EN_PROCESO' AND r.ultima_ejecucion <= ?)
                    ORDER BY r.proxima_ejecucion
                    FOR UPDATE SKIP LOCKED
                    LIMIT 20
                ), reclamadas AS (
                    UPDATE public.reembolso_pago r
                    SET estado = 'EN_PROCESO', intentos = r.intentos + 1,
                        ultima_ejecucion = ?, fecha_actualizacion = ?
                    FROM candidatas c
                    WHERE r.id_reembolso = c.id_reembolso
                    RETURNING r.id_reembolso, r.id_pago, r.payment_intent_id,
                              r.stripe_refund_id, r.clave_idempotencia
                )
                SELECT r.id_reembolso, r.payment_intent_id, r.stripe_refund_id,
                       r.clave_idempotencia, p.monto
                FROM reclamadas r
                JOIN public.pago p ON p.id_pago = r.id_pago
                """;
        OffsetDateTime recuperacion = ahora.minusMinutes(5);
        return jdbc.query(sql, (rs, row) -> new ReembolsoTrabajo(
                rs.getObject("id_reembolso", UUID.class),
                rs.getString("payment_intent_id"),
                rs.getString("stripe_refund_id"),
                rs.getString("clave_idempotencia"),
                rs.getBigDecimal("monto")), ahora, recuperacion, ahora, ahora);
    }

    @Transactional
    public void confirmar(UUID idReembolso, String referenciaStripe, OffsetDateTime ahora) {
        ReembolsoPago reembolso = reembolsos.findById(idReembolso)
                .orElseThrow(() -> new IllegalStateException("No existe el reembolso pendiente."));
        if ("REEMBOLSADO".equals(reembolso.getEstado())) return;
        reembolso.setStripeRefundId(referenciaStripe);
        reembolso.setEstado("REEMBOLSADO");
        reembolso.setUltimoError(null);
        reembolso.setFechaActualizacion(ahora);
        reembolsos.save(reembolso);
        Pago pago = pagos.findById(reembolso.getIdPago())
                .orElseThrow(() -> new IllegalStateException("No existe el pago asociado al reembolso."));
        pago.setEstado("REEMBOLSADO");
        pagos.save(pago);
    }

    @Transactional
    public void reprogramar(UUID idReembolso, String referenciaStripe, String error, OffsetDateTime ahora) {
        ReembolsoPago reembolso = reembolsos.findById(idReembolso)
                .orElseThrow(() -> new IllegalStateException("No existe el reembolso pendiente."));
        reembolso.setStripeRefundId(referenciaStripe);
        reembolso.setUltimoError(limitar(error));
        if (reembolso.getIntentos() >= MAXIMO_INTENTOS) {
            reembolso.setEstado("FALLIDA");
        } else {
            long segundos = Math.min(3600L, 30L * (1L << Math.min(7, reembolso.getIntentos() - 1)));
            reembolso.setEstado("PENDIENTE");
            reembolso.setProximaEjecucion(ahora.plusSeconds(segundos));
        }
        reembolso.setFechaActualizacion(ahora);
        reembolsos.save(reembolso);
    }

    @Transactional
    public void marcarFallaDefinitiva(UUID idReembolso, String referenciaStripe, String error, OffsetDateTime ahora) {
        ReembolsoPago reembolso = reembolsos.findById(idReembolso)
                .orElseThrow(() -> new IllegalStateException("No existe el reembolso pendiente."));
        reembolso.setStripeRefundId(referenciaStripe);
        reembolso.setEstado("FALLIDA");
        reembolso.setUltimoError(limitar(error));
        reembolso.setFechaActualizacion(ahora);
        reembolsos.save(reembolso);
    }

    private String limitar(String texto) {
        if (texto == null || texto.isBlank()) return "Stripe no confirmó el reembolso.";
        return texto.length() > 2000 ? texto.substring(0, 2000) : texto;
    }
}
