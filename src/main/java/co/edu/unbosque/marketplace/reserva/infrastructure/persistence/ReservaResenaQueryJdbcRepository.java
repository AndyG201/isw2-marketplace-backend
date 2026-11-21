package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.reserva.application.port.ReservaResenaQueryRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaInfo;

@Repository
public class ReservaResenaQueryJdbcRepository implements ReservaResenaQueryRepository {

    private final JdbcTemplate jdbc;
    private final RowMapper<ReservaResenaInfo> mapper = this::mapear;

    public ReservaResenaQueryJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ReservaResenaInfo> consultar(UUID idReserva) {
        List<ReservaResenaInfo> resultado = jdbc.query("""
                SELECT r.id_reserva AS "idReserva",
                       r.id_huesped AS "idHuesped",
                       r.estado AS "estado",
                       c.id_publicacion AS "idPublicacion",
                       p.id_alojamiento AS "idAlojamiento"
                FROM public.reserva r
                JOIN public.vista_cotizacion c ON c.id_cotizacion = r.id_cotizacion
                JOIN public.vista_publicacion p ON p.id_publicacion = c.id_publicacion
                WHERE r.id_reserva = ?
                """, mapper, idReserva);
        return resultado.stream().findFirst();
    }

    private ReservaResenaInfo mapear(ResultSet rs, int fila) throws SQLException {
        return new ReservaResenaInfo(
                rs.getObject("idReserva", UUID.class),
                rs.getObject("idHuesped", UUID.class),
                rs.getObject("idPublicacion", UUID.class),
                rs.getObject("idAlojamiento", UUID.class),
                rs.getString("estado"));
    }
}
