package co.edu.unbosque.marketplace.reserva.contracts;

import java.util.UUID;

public record ReservaAfectadaInfo(
        UUID idReserva,
        UUID idHuesped,
        UUID idCotizacion,
        String estado
) {
}
