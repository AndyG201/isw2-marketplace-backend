package co.edu.unbosque.marketplace.notificacion.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionAlojamientoQuery;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaParticipantesQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioAdministracionQuery;

@Component
public class DestinatariosNotificacion {

    private final ReservaParticipantesQuery reservas;
    private final PublicacionAlojamientoQuery publicaciones;
    private final AlojamientoPublicacionQuery alojamientos;
    private final UsuarioAdministracionQuery usuarios;

    public DestinatariosNotificacion(
            ReservaParticipantesQuery reservas,
            PublicacionAlojamientoQuery publicaciones,
            AlojamientoPublicacionQuery alojamientos,
            UsuarioAdministracionQuery usuarios) {
        this.reservas = reservas;
        this.publicaciones = publicaciones;
        this.alojamientos = alojamientos;
        this.usuarios = usuarios;
    }

    public Optional<DestinatariosReserva> buscarDestinatariosReserva(UUID idReserva) {
        return reservas.consultarParticipantes(idReserva)
                .map(participantes -> new DestinatariosReserva(
                        participantes.idHuesped(),
                        alojamientos.consultarIdAnfitrion(participantes.idAlojamiento()).orElse(null),
                        participantes.idPublicacion()));
    }

    public Optional<UUID> buscarAnfitrionPublicacion(UUID idPublicacion) {
        return publicaciones.consultarIdAlojamiento(idPublicacion)
                .flatMap(alojamientos::consultarIdAnfitrion);
    }

    public List<UUID> buscarAdministradoresActivos() {
        return usuarios.consultarAdministradoresActivos();
    }

    public record DestinatariosReserva(UUID idHuesped, UUID idAnfitrion, UUID idPublicacion) {
    }
}
