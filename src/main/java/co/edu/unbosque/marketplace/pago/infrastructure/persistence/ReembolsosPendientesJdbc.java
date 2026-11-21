package co.edu.unbosque.marketplace.pago.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.pago.application.ReembolsoTrabajo;
import co.edu.unbosque.marketplace.pago.application.port.ReembolsosPendientes;

@Repository
public class ReembolsosPendientesJdbc implements ReembolsosPendientes {

    private static final String RECLAMAR = """
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

    private final JdbcTemplate jdbc;

    public ReembolsosPendientesJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ReembolsoTrabajo> reclamar(OffsetDateTime ahora) {
        OffsetDateTime recuperacion = ahora.minusMinutes(5);
        return jdbc.query(RECLAMAR, (rs, row) -> new ReembolsoTrabajo(
                rs.getObject("id_reembolso", UUID.class),
                rs.getString("payment_intent_id"),
                rs.getString("stripe_refund_id"),
                rs.getString("clave_idempotencia"),
                rs.getBigDecimal("monto")), ahora, recuperacion, ahora, ahora);
    }
}
