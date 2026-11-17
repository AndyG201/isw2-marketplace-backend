package co.edu.unbosque.marketplace.reserva.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaRetenidaEvent(UUID idAnfitrion, UUID idHuesped, UUID idReserva,
        UUID idPublicacion, OffsetDateTime fechaVencimiento) {
}
