package co.edu.unbosque.marketplace.disponibilidad.api.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CalendarioDisponibilidadResponse(
        UUID idAlojamiento,
        List<PeriodoDisponibilidadResponse> periodos,
        List<LocalDate> nochesOcupadas
) {

    public CalendarioDisponibilidadResponse {
        periodos = List.copyOf(periodos);
        nochesOcupadas = List.copyOf(nochesOcupadas);
    }
}
