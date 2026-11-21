package co.edu.unbosque.marketplace.reserva.contracts;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NochesOcupadasQuery {

    boolean hayNochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta);

    List<LocalDate> nochesOcupadas(UUID idAlojamiento, LocalDate desde, LocalDate hasta);

    List<LocalDate> nochesOcupadas(UUID idAlojamiento);
}
