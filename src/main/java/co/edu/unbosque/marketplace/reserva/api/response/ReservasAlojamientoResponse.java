package co.edu.unbosque.marketplace.reserva.api.response;

import java.util.List;

public record ReservasAlojamientoResponse(
        List<ReservaAlojamientoResponse> reservas,
        int totalResultados,
        String mensaje) {
}
