package co.edu.unbosque.marketplace.reserva.application;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaQuery;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;

@Service
public class ReservaAfectadaQueryService
        implements ReservaAfectadaQuery {

    private final ReservaRepository reservaRepository;

    public ReservaAfectadaQueryService(
            ReservaRepository reservaRepository) {

        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAfectadaInfo> consultarPorCotizaciones(
            Collection<UUID> idsCotizaciones) {

        if (idsCotizaciones == null || idsCotizaciones.isEmpty()) {
            return List.of();
        }

        return reservaRepository
                .findByIdCotizacionIn(idsCotizaciones)
                .stream()
                .map(this::convertirInformacion)
                .toList();
    }

    private ReservaAfectadaInfo convertirInformacion(
            Reserva reserva) {

        return new ReservaAfectadaInfo(
                reserva.getIdReserva(),
                reserva.getIdHuesped(),
                reserva.getIdCotizacion(),
                reserva.getEstado()
        );
    }
}
