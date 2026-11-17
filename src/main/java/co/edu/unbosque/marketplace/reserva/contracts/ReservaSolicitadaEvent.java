package co.edu.unbosque.marketplace.reserva.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaSolicitadaEvent(
        UUID idAnfitrion,
        UUID idReserva,
        UUID idPublicacion,
        String modoAceptacion,
        OffsetDateTime fechaEvento) {
}