package co.edu.unbosque.marketplace.disponibilidad.api.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ConsultaDisponibilidadResponse(
        UUID idAlojamiento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean disponible,
        boolean cubiertoPorPeriodosDisponibles,
        List<LocalDate> nochesOcupadas
) {

    public ConsultaDisponibilidadResponse {
        nochesOcupadas = List.copyOf(nochesOcupadas);
    }
}
