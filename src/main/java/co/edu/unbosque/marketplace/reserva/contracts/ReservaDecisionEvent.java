package co.edu.unbosque.marketplace.reserva.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaDecisionEvent(
        UUID idHuesped,
        UUID idReserva,
        UUID idPublicacion,
        String decision,
        OffsetDateTime fechaDecision,
        OffsetDateTime retencionHasta,
        OffsetDateTime fechaLimiteAprobacion) {
}