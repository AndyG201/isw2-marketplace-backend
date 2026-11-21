package co.edu.unbosque.marketplace.reserva.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;

@Service
public class ReservaCompletadaService {

    private static final String COMPLETADA = "COMPLETADA";

    private final ReservaRepository reservas;

    public ReservaCompletadaService(ReservaRepository reservas) {
        this.reservas = reservas;
    }

    @Transactional
    public int completarReservasFinalizadas(LocalDate hoy) {
        List<Reserva> finalizadas = reservas.buscarConfirmadasFinalizadas(hoy);
        finalizadas.forEach(reserva -> reserva.setEstado(COMPLETADA));
        reservas.saveAllAndFlush(finalizadas);
        return finalizadas.size();
    }
}
