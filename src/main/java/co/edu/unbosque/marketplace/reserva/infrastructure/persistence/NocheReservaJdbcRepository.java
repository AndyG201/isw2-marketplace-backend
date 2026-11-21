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
    private final JdbcTemplate jdbc;

    public NocheReservaJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean reservarRango(UUID idAlojamiento, UUID idReserva, LocalDate entrada, LocalDate salida) {
        jdbc.queryForObject("SELECT id_alojamiento FROM public.alojamiento WHERE id_alojamiento = ? FOR UPDATE",
                UUID.class, idAlojamiento);
        Boolean disponible = jdbc.queryForObject("""
                SELECT NOT EXISTS (
                    SELECT 1 FROM generate_series(?::date, (?::date - 1), interval '1 day') f(noche)
                    WHERE NOT EXISTS (
                        SELECT 1 FROM public.periodo_disponibilidad pd
                        WHERE pd.id_alojamiento = ? AND pd.habilitado
                          AND pd.fecha_inicio <= f.noche::date AND pd.fecha_fin > f.noche::date)
                    OR EXISTS (
                        SELECT 1 FROM public.noche_reservada nr
                        JOIN public.reserva_noche rn ON rn.id_noche = nr.id_noche
                        JOIN public.reserva r ON r.id_reserva = rn.id_reserva
                        WHERE nr.id_alojamiento = ? AND nr.fecha = f.noche::date
                          AND (r.estado IN ('PENDIENTE_APROBACION','CONFIRMADA','COMPLETADA')
                               OR (r.estado = 'PENDIENTE_PAGO' AND r.retencion_hasta > CURRENT_TIMESTAMP))
                          AND r.id_reserva <> ?))
                """, Boolean.class, Date.valueOf(entrada), Date.valueOf(salida),
                idAlojamiento, idAlojamiento, idReserva);
        if (!Boolean.TRUE.equals(disponible)) {
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
}
