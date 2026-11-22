package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaCancelacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaCancelacionInfo;

@Repository
public class ReservaCancelacionJdbcRepository implements ReservaCancelacionRepository {

    private static final String MARCAR_CANCELADA = """
            UPDATE public.reserva
            SET estado = 'CANCELADA', retencion_hasta = NULL
            WHERE id_reserva = ?
              AND estado IN ('PENDIENTE_PAGO', 'PENDIENTE_APROBACION', 'CONFIRMADA')
            """;

    private static final String CONSULTAR_Y_BLOQUEAR = """
            SELECT r.id_reserva AS "idReserva",
                   r.id_huesped AS "idHuesped",
                   r.estado AS "estado",
                   r.id_politica_aplicada AS "idPoliticaAplicada",
                   c.fecha_entrada AS "fechaEntrada",
                   c.monto_total AS "montoTotal"
            FROM public.reserva r
            JOIN public.vista_cotizacion c ON c.id_cotizacion = r.id_cotizacion
            WHERE r.id_reserva = ?
            FOR UPDATE OF r
            """;

    private final JdbcTemplate jdbc;
    private final RowMapper<ReservaCancelacionInfo> mapper = this::mapear;

    public ReservaCancelacionJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ReservaCancelacionInfo> bloquearYConsultar(UUID idReserva) {
        List<ReservaCancelacionInfo> resultado = jdbc.query(CONSULTAR_Y_BLOQUEAR, mapper, idReserva);
        return resultado.stream().findFirst();
    }

    @Override
    public void marcarCancelada(UUID idReserva) {
        int actualizadas = jdbc.update(MARCAR_CANCELADA, idReserva);
        if (actualizadas != 1) {
            throw new ConflictoNegocioException("La reserva cambió de estado durante la cancelación.");
        }
    }

    private ReservaCancelacionInfo mapear(ResultSet rs, int fila) throws SQLException {
        return new ReservaCancelacionInfo(
                rs.getObject("idReserva", UUID.class),
                rs.getObject("idHuesped", UUID.class),
                rs.getString("estado"),
                rs.getObject("idPoliticaAplicada", UUID.class),
                rs.getObject("fechaEntrada", LocalDate.class),
                rs.getBigDecimal("montoTotal"),
                null,
                null);
    }
}
