package co.edu.unbosque.marketplace.reserva.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DecisionReservaResponse(
        UUID idReserva,
        String estado,
        OffsetDateTime fechaDecision,
        OffsetDateTime retencionHasta,
        String mensaje) {
}