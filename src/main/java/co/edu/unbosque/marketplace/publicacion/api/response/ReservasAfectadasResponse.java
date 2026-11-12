package co.edu.unbosque.marketplace.publicacion.api.response;

import java.util.List;

public record ReservasAfectadasResponse(
        String mensaje,
        List<ReservaAfectadaResponse> reservas
) {

    public ReservasAfectadasResponse {
        reservas = reservas == null
                ? List.of()
                : List.copyOf(reservas);
    }
}
