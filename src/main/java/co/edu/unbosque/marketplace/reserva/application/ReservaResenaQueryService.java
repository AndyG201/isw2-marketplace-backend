package co.edu.unbosque.marketplace.reserva.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.application.port.ReservaResenaQueryRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaParticipantes;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaParticipantesQuery;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaQuery;

@Service
public class ReservaResenaQueryService implements ReservaResenaQuery, ReservaParticipantesQuery {

    private final ReservaResenaQueryRepository reservas;

    public ReservaResenaQueryService(ReservaResenaQueryRepository reservas) {
        this.reservas = reservas;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaResenaInfo> consultarReservaParaResena(UUID idReserva) {
        if (idReserva == null) {
            return Optional.empty();
        }
        return reservas.consultar(idReserva);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaParticipantes> consultarParticipantes(UUID idReserva) {
        return consultarReservaParaResena(idReserva)
                .map(reserva -> new ReservaParticipantes(
                        reserva.idHuesped(), reserva.idPublicacion(), reserva.idAlojamiento()));
    }
}
