package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;

@Repository
public class NocheReservaJdbcRepository implements NocheReservaRepository {

    private static final String NOCHES_OCUPADAS = """
            FROM public.noche_reservada nr
            JOIN public.reserva_noche rn ON rn.id_noche = nr.id_noche
            JOIN public.reserva r ON r.id_reserva = rn.id_reserva
            WHERE nr.id_alojamiento = ?
              AND (r.estado IN ('PENDIENTE_APROBACION', 'CONFIRMADA', 'COMPLETADA')
                   OR (r.estado = 'PENDIENTE_PAGO' AND r.retencion_hasta > CURRENT_TIMESTAMP))
            """;

    private static final String EN_RANGO = " AND nr.fecha >= ? AND nr.fecha < ?";

    private static final String OCUPADA_POR_OTRA_RESERVA =
            "SELECT EXISTS (SELECT 1 " + NOCHES_OCUPADAS + EN_RANGO + " AND r.id_reserva <> ?)";

    private static final String OCUPADA_EN_RANGO = "SELECT EXISTS (SELECT 1 " + NOCHES_OCUPADAS + EN_RANGO + ")";

    private static final String FECHAS_EN_RANGO =
            "SELECT DISTINCT nr.fecha " + NOCHES_OCUPADAS + EN_RANGO + " ORDER BY nr.fecha";

    private static final String TODAS_LAS_FECHAS = "SELECT DISTINCT nr.fecha " + NOCHES_OCUPADAS + " ORDER BY nr.fecha";

    private final JdbcTemplate jdbc;

    public NocheReservaJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean reservarRango(UUID idAlojamiento, UUID idReserva, LocalDate entrada, LocalDate salida) {
        jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                resultado -> {
                    resultado.next();
                    return null;
                },
                "noches:" + idAlojamiento);
        Boolean ocupado = jdbc.queryForObject(
                OCUPADA_POR_OTRA_RESERVA, Boolean.class,
                idAlojamiento, Date.valueOf(entrada), Date.valueOf(salida), idReserva);
        if (Boolean.TRUE.equals(ocupado)) {
            return false;
        }
        for (LocalDate fecha = entrada; fecha.isBefore(salida); fecha = fecha.plusDays(1)) {
            UUID idNoche = UUID.randomUUID();
            jdbc.update("INSERT INTO public.noche_reservada (id_noche,id_alojamiento,fecha) VALUES (?,?,?)",
                    idNoche, idAlojamiento, Date.valueOf(fecha));
            jdbc.update("INSERT INTO public.reserva_noche (id_noche,id_reserva) VALUES (?,?)", idNoche, idReserva);
        }
        return true;
    }

    @Override
    public void liberar(UUID idReserva) {
        List<UUID> idsNoches = jdbc.query(
                "SELECT id_noche FROM public.reserva_noche WHERE id_reserva = ?",
                (rs, rowNum) -> rs.getObject("id_noche", UUID.class), idReserva);
        if (idsNoches.isEmpty()) {
            return;
        }
        jdbc.update("DELETE FROM public.reserva_noche WHERE id_reserva = ?", idReserva);
        String parametros = String.join(",", Collections.nCopies(idsNoches.size(), "?"));
        jdbc.update("DELETE FROM public.noche_reservada WHERE id_noche IN (" + parametros + ")",
                idsNoches.toArray());
    }

    @Override
    public boolean hayNochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                OCUPADA_EN_RANGO, Boolean.class, idAlojamiento, Date.valueOf(desde), Date.valueOf(hasta)));
    }

    @Override
    public List<LocalDate> nochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList(
                FECHAS_EN_RANGO, LocalDate.class, idAlojamiento, Date.valueOf(desde), Date.valueOf(hasta));
    }

    @Override
    public List<LocalDate> nochesOcupadas(UUID idAlojamiento) {
        return jdbc.queryForList(
                TODAS_LAS_FECHAS, LocalDate.class, idAlojamiento);
    }
}
