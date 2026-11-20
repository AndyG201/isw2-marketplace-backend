package co.edu.unbosque.marketplace.notificacion.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class NotificacionDestinatarioRepository {

    private final JdbcTemplate jdbc;
    private final RowMapper<DestinatariosReserva> reservaMapper = this::mapearReserva;

    public NotificacionDestinatarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<DestinatariosReserva> buscarDestinatariosReserva(UUID idReserva) {
        List<DestinatariosReserva> resultado = jdbc.query("""
                SELECT r.id_huesped AS "idHuesped",
                       a.id_anfitrion AS "idAnfitrion",
                       c.id_publicacion AS "idPublicacion"
                FROM public.reserva r
                JOIN public.cotizacion c ON c.id_cotizacion = r.id_cotizacion
                JOIN public.publicacion p ON p.id_publicacion = c.id_publicacion
                JOIN public.alojamiento a ON a.id_alojamiento = p.id_alojamiento
                WHERE r.id_reserva = ?
                """, reservaMapper, idReserva);
        return resultado.stream().findFirst();
    }

    public Optional<UUID> buscarAnfitrionPublicacion(UUID idPublicacion) {
        return jdbc.query("""
                SELECT a.id_anfitrion
                FROM public.publicacion p
                JOIN public.alojamiento a ON a.id_alojamiento = p.id_alojamiento
                WHERE p.id_publicacion = ?
                """, (rs, fila) -> rs.getObject("id_anfitrion", UUID.class), idPublicacion)
                .stream()
                .findFirst();
    }

    public List<UUID> buscarAdministradoresActivos() {
        return jdbc.query("""
                SELECT u.id_usuario
                FROM public.usuario u
                JOIN public.rol r ON r.id_rol = u.id_rol
                WHERE r.nombre = 'ADMINISTRADOR'
                  AND u.estado = 'ACTIVA'
                  AND u.restringido = false
                """, (rs, fila) -> rs.getObject("id_usuario", UUID.class));
    }

    private DestinatariosReserva mapearReserva(ResultSet rs, int fila) throws SQLException {
        return new DestinatariosReserva(
                rs.getObject("idHuesped", UUID.class),
                rs.getObject("idAnfitrion", UUID.class),
                rs.getObject("idPublicacion", UUID.class));
    }

    public record DestinatariosReserva(UUID idHuesped, UUID idAnfitrion, UUID idPublicacion) {
    }
}
