package co.edu.unbosque.marketplace.disponibilidad.api.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PeriodoDisponibilidadResponse(
        UUID idPeriodo,
        UUID idAlojamiento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean habilitado,
        OffsetDateTime fechaCreacion
) {
}
