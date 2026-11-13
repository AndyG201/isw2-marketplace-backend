package co.edu.unbosque.marketplace.publicacion.api.response;

import java.time.LocalDate;
import java.util.UUID;

public record ReservaAfectadaResponse(
        UUID idReserva,
        UUID idPublicacion,
        String tituloPublicacion,
        UUID idAlojamiento,
        String nombreHuesped,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String estadoReserva,
        String motivoBloqueo
) {
}
