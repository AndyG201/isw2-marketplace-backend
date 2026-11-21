package co.edu.unbosque.marketplace.reserva.application.dto;

import java.util.List;

public record ReservasAlojamientoResponse(
        List<ReservaAlojamientoResponse> reservas,
        int totalResultados,
        String mensaje) {
}
