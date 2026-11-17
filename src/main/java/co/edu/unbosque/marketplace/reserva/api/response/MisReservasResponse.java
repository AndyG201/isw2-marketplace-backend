package co.edu.unbosque.marketplace.reserva.api.response;

import java.util.List;

public record MisReservasResponse(
        List<ReservaPropiaResponse> reservas,
        int totalResultados,
        String mensaje) {
}