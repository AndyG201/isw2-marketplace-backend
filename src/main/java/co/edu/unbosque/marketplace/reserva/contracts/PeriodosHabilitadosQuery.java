package co.edu.unbosque.marketplace.reserva.contracts;

import java.time.LocalDate;
import java.util.UUID;

public interface PeriodosHabilitadosQuery {

    boolean rangoHabilitado(UUID idAlojamiento, LocalDate fechaEntrada, LocalDate fechaSalida);
}
