package co.edu.unbosque.marketplace.reserva.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NocheReservaRepository {

    boolean reservarRango(UUID idAlojamiento, UUID idReserva, LocalDate entrada, LocalDate salida);

    void liberar(UUID idReserva);

    boolean hayNochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta);

    List<LocalDate> nochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta);

    List<LocalDate> nochesOcupadas(UUID idAlojamiento);
}
