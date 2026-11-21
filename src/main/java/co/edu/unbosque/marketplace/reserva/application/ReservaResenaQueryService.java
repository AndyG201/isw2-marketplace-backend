package co.edu.unbosque.marketplace.reserva.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaQuery;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaResenaQueryRepository;

@Service
public class ReservaResenaQueryService implements ReservaResenaQuery {

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
}
