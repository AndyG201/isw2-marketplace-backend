package co.edu.unbosque.marketplace.reserva.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaRetencionVencidaEvent(UUID idHuesped, UUID idReserva, UUID idPublicacion,
        OffsetDateTime fechaEvento, String motivo) {
}
