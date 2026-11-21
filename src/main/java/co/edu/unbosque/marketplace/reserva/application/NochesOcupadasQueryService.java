package co.edu.unbosque.marketplace.reserva.application;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.contracts.NochesOcupadasQuery;

@Service
public class NochesOcupadasQueryService implements NochesOcupadasQuery {

    private final NocheReservaRepository noches;

    public NochesOcupadasQueryService(NocheReservaRepository noches) {
        this.noches = noches;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayNochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta) {
        return noches.hayNochesOcupadas(idAlojamiento, desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> nochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta) {
        return noches.nochesOcupadas(idAlojamiento, desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> nochesOcupadas(UUID idAlojamiento) {
        return noches.nochesOcupadas(idAlojamiento);
    }
}
