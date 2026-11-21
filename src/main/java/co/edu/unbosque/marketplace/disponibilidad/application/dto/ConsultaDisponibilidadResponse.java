package co.edu.unbosque.marketplace.disponibilidad.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ConsultaDisponibilidadResponse(
        UUID idAlojamiento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean disponible,
        boolean cubiertoPorPeriodosDisponibles,
        List<LocalDate> nochesOcupadas,
        List<RangoDisponibleResponse> periodosDisponibles
) {

    public ConsultaDisponibilidadResponse {
        nochesOcupadas = List.copyOf(nochesOcupadas);
        periodosDisponibles = List.copyOf(periodosDisponibles);
    }
}
