package co.edu.unbosque.marketplace.resena.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.resena.application.port.ResenaPublicaQueryRepository;
import co.edu.unbosque.marketplace.resena.domain.Resena;

@Repository
public class ResenaPublicaQueryJdbcRepository implements ResenaPublicaQueryRepository {

    private final JdbcTemplate jdbc;
    private final RowMapper<Resena> mapper = this::mapear;

    public ResenaPublicaQueryJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existePublicacion(UUID idPublicacion) {
        Boolean existe = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM public.publicacion WHERE id_publicacion = ?)",
                Boolean.class,
                idPublicacion);
        return Boolean.TRUE.equals(existe);
    }

    @Override
    public List<Resena> consultarVisibles(UUID idPublicacion) {
        return jdbc.query("""
                SELECT r.id_resena,
                       r.id_reserva,
                       r.calificacion,
                       r.comentario,
                       r.visible,
                       r.fecha_publicacion
                FROM public.resena r
                JOIN public.reserva re ON re.id_reserva = r.id_reserva
                JOIN public.cotizacion c ON c.id_cotizacion = re.id_cotizacion
                WHERE c.id_publicacion = ?
                  AND r.visible = TRUE
                ORDER BY r.fecha_publicacion DESC, r.id_resena
                """, mapper, idPublicacion);
    }

    private Resena mapear(ResultSet rs, int fila) throws SQLException {
        Resena resena = new Resena();
        resena.setIdResena(rs.getObject("id_resena", UUID.class));
        resena.setIdReserva(rs.getObject("id_reserva", UUID.class));
        resena.setCalificacion(rs.getInt("calificacion"));
        resena.setComentario(rs.getString("comentario"));
        resena.setVisible(rs.getBoolean("visible"));
        resena.setFechaPublicacion(rs.getObject("fecha_publicacion", java.time.OffsetDateTime.class));
        return resena;
    }
}
