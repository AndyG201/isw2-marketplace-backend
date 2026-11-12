package co.edu.unbosque.marketplace.cotizacion.contracts;

import java.time.LocalDate;
import java.util.UUID;

public record CotizacionReservaInfo(
        UUID idCotizacion,
        UUID idPublicacion,
        LocalDate fechaEntrada,
        LocalDate fechaSalida
) {
}
