package co.edu.unbosque.marketplace.reserva.contracts;

import java.util.UUID;

public record ReservaResenaInfo(
        UUID idReserva,
        UUID idHuesped,
        UUID idPublicacion,
        UUID idAlojamiento,
        String estado) {
}
