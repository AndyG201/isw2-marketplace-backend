package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.contracts.EstadoReserva;

@Repository
public class NocheReservaJdbcRepository implements NocheReservaRepository {

    private static final String OCUPAN = EstadoReserva.enSql(EstadoReserva.QUE_OCUPAN_NOCHES);
    private static final String CON_RETENCION = EstadoReserva.enSql(EstadoReserva.CON_RETENCION);
    private static final String LIBERAN = EstadoReserva.enSql(EstadoReserva.QUE_LIBERAN_NOCHES);

    private static final String NOCHES_OCUPADAS = """
            FROM public.noche_reservada nr
            JOIN public.reserva_noche rn ON rn.id_noche = nr.id_noche
            JOIN public.reserva r ON r.id_reserva = rn.id_reserva
            WHERE nr.id_alojamiento = ?
              AND (
                    r.estado IN (%1$s)
                    OR (
                        r.estado IN (%2$s)
                        AND r.retencion_hasta IS NOT NULL
                        AND r.retencion_hasta > CURRENT_TIMESTAMP
                    )
              )
            """.formatted(OCUPAN, CON_RETENCION, LIBERAN);

    private static final String EN_RANGO = " AND nr.fecha >= ? AND nr.fecha < ?";

    private static final String OCUPADA_POR_OTRA_RESERVA =
            "SELECT EXISTS (SELECT 1 " + NOCHES_OCUPADAS + EN_RANGO + " AND r.id_reserva <> ?)";

    private static final String OCUPADA_EN_RANGO =
            "SELECT EXISTS (SELECT 1 " + NOCHES_OCUPADAS + EN_RANGO + ")";

    private static final String FECHAS_EN_RANGO =
            "SELECT DISTINCT nr.fecha " + NOCHES_OCUPADAS + EN_RANGO + " ORDER BY nr.fecha";

    private static final String TODAS_LAS_FECHAS =
            "SELECT DISTINCT nr.fecha " + NOCHES_OCUPADAS + " ORDER BY nr.fecha";

    /**
     * Elimina vínculos de noches que ya no representan una retención vigente.
     * SKIP LOCKED evita bloquear una nueva retención si el scheduler u otra
     * operación ya está procesando una de las reservas vencidas. En ese caso,
     * la comprobación física posterior hará que la operación responda como
     * conflicto temporal, en lugar de intentar insertar y provocar una violación
     * de la restricción uq_noche_alojamiento_fecha.
     */
    private static final String LIMPIAR_NOCHES_VENCIDAS = """
            WITH noches_vencidas AS MATERIALIZED (
                SELECT nr.id_noche
                FROM public.noche_reservada nr
                JOIN public.reserva_noche rn ON rn.id_noche = nr.id_noche
                JOIN public.reserva r ON r.id_reserva = rn.id_reserva
                WHERE nr.id_alojamiento = ?
                  AND (
                        r.estado IN (%3$s)
                        OR (
                            r.estado IN (%2$s)
                            AND (
                                r.retencion_hasta IS NULL
                                OR r.retencion_hasta <= CURRENT_TIMESTAMP
                            )
                        )
                  )
                FOR UPDATE OF r, nr SKIP LOCKED
            ),
            enlaces_eliminados AS (
                DELETE FROM public.reserva_noche rn
                USING noches_vencidas nv
                WHERE rn.id_noche = nv.id_noche
                RETURNING rn.id_noche
            )
            DELETE FROM public.noche_reservada nr
            USING enlaces_eliminados e
            WHERE nr.id_noche = e.id_noche
            """.formatted(OCUPAN, CON_RETENCION, LIBERAN);

    private static final String LIMPIAR_NOCHES_HUERFANAS = """
            DELETE FROM public.noche_reservada nr
            WHERE nr.id_alojamiento = ?
              AND NOT EXISTS (
                    SELECT 1
                    FROM public.reserva_noche rn
                    WHERE rn.id_noche = nr.id_noche
              )
            """;

    private static final String CONTAR_NOCHES_RESERVA = """
            SELECT COUNT(*)
            FROM public.reserva_noche rn
            JOIN public.noche_reservada nr ON nr.id_noche = rn.id_noche
            WHERE rn.id_reserva = ?
            """;

    private static final String CONTAR_NOCHES_RESERVA_EN_RANGO = """
            SELECT COUNT(*)
            FROM public.reserva_noche rn
            JOIN public.noche_reservada nr ON nr.id_noche = rn.id_noche
            WHERE rn.id_reserva = ?
              AND nr.id_alojamiento = ?
              AND nr.fecha >= ?
              AND nr.fecha < ?
            """;

    private static final String EXISTE_NOCHE_FISICA_EN_RANGO = """
            SELECT EXISTS (
                SELECT 1
                FROM public.noche_reservada nr
                WHERE nr.id_alojamiento = ?
                  AND nr.fecha >= ?
                  AND nr.fecha < ?
            )
            """;

    private static final String RESERVA_CON_RETENCION_VIGENTE = """
            SELECT EXISTS (
                SELECT 1
                FROM public.reserva r
                WHERE r.id_reserva = ?
                  AND (
                        r.estado IN (%1$s)
                        OR (
                            r.estado IN (%2$s)
                            AND r.retencion_hasta > CURRENT_TIMESTAMP
                        )
                  )
            )
            """.formatted(OCUPAN, CON_RETENCION, LIBERAN);

    private final JdbcTemplate jdbc;

    public NocheReservaJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean reservarRango(
            UUID idAlojamiento,
            UUID idReserva,
            LocalDate entrada,
            LocalDate salida) {

        bloquearAlojamiento(idAlojamiento);

        // Primero se reconcilian las filas antiguas dentro de la misma
        // transacción y bajo el mismo bloqueo usado para crear nuevas noches.
        jdbc.update(LIMPIAR_NOCHES_VENCIDAS, idAlojamiento);
        jdbc.update(LIMPIAR_NOCHES_HUERFANAS, idAlojamiento);

        long nochesEsperadas = ChronoUnit.DAYS.between(entrada, salida);
        if (nochesEsperadas <= 0) {
            return false;
        }

        long nochesActualesReserva = jdbc.queryForObject(
                CONTAR_NOCHES_RESERVA,
                Long.class,
                idReserva);

        if (nochesActualesReserva > 0) {
            long nochesEnRango = jdbc.queryForObject(
                    CONTAR_NOCHES_RESERVA_EN_RANGO,
                    Long.class,
                    idReserva,
                    idAlojamiento,
                    Date.valueOf(entrada),
                    Date.valueOf(salida));
            Boolean retencionVigente = jdbc.queryForObject(
                    RESERVA_CON_RETENCION_VIGENTE,
                    Boolean.class,
                    idReserva);

            // Repetir la misma retención solo es idempotente si la reserva sigue
            // vigente. Una fila física vencida o una retención parcial nunca se
            // interpreta como una reserva válida.
            return Boolean.TRUE.equals(retencionVigente)
                    && nochesActualesReserva == nochesEsperadas
                    && nochesEnRango == nochesEsperadas;
        }

        Boolean ocupado = jdbc.queryForObject(
                OCUPADA_POR_OTRA_RESERVA,
                Boolean.class,
                idAlojamiento,
                Date.valueOf(entrada),
                Date.valueOf(salida),
                idReserva);

        if (Boolean.TRUE.equals(ocupado)) {
            return false;
        }

        // Una reserva vencida que esté bloqueada por el scheduler puede haber
        // quedado fuera de la limpieza por SKIP LOCKED. No se intenta insertar
        // encima de su fila física: se devuelve conflicto y se evita HTTP 500.
        Boolean existeFilaFisica = jdbc.queryForObject(
                EXISTE_NOCHE_FISICA_EN_RANGO,
                Boolean.class,
                idAlojamiento,
                Date.valueOf(entrada),
                Date.valueOf(salida));

        if (Boolean.TRUE.equals(existeFilaFisica)) {
            return false;
        }

        for (LocalDate fecha = entrada; fecha.isBefore(salida); fecha = fecha.plusDays(1)) {
            UUID idNoche = UUID.randomUUID();
            jdbc.update(
                    "INSERT INTO public.noche_reservada (id_noche,id_alojamiento,fecha) VALUES (?,?,?)",
                    idNoche,
                    idAlojamiento,
                    Date.valueOf(fecha));
            jdbc.update(
                    "INSERT INTO public.reserva_noche (id_noche,id_reserva) VALUES (?,?)",
                    idNoche,
                    idReserva);
        }

        return true;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void liberar(UUID idReserva) {
        List<UUID> idsNoches = jdbc.query(
                "SELECT id_noche FROM public.reserva_noche WHERE id_reserva = ?",
                (rs, rowNum) -> rs.getObject("id_noche", UUID.class),
                idReserva);

        if (idsNoches.isEmpty()) {
            return;
        }

        jdbc.update(
                "DELETE FROM public.reserva_noche WHERE id_reserva = ?",
                idReserva);

        String parametros = String.join(",", Collections.nCopies(idsNoches.size(), "?"));
        jdbc.update(
                "DELETE FROM public.noche_reservada WHERE id_noche IN (" + parametros + ")",
                idsNoches.toArray());
    }

    @Override
    public boolean hayNochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                OCUPADA_EN_RANGO,
                Boolean.class,
                idAlojamiento,
                Date.valueOf(desde),
                Date.valueOf(hasta)));
    }

    @Override
    public List<LocalDate> nochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList(
                FECHAS_EN_RANGO,
                LocalDate.class,
                idAlojamiento,
                Date.valueOf(desde),
                Date.valueOf(hasta));
    }

    @Override
    public List<LocalDate> nochesOcupadas(UUID idAlojamiento) {
        return jdbc.queryForList(
                TODAS_LAS_FECHAS,
                LocalDate.class,
                idAlojamiento);
    }

    private void bloquearAlojamiento(UUID idAlojamiento) {
        jdbc.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                resultado -> {
                    resultado.next();
                    return null;
                },
                "noches:" + idAlojamiento);
    }
}
