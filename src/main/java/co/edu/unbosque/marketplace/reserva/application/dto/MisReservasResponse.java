package co.edu.unbosque.marketplace.reserva.application.dto;

import java.util.List;

public record MisReservasResponse(
        List<ReservaPropiaResponse> reservas,
        int totalResultados,
        String mensaje) {
}