package co.edu.unbosque.marketplace.resena.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OcultarResenaResponse(
        UUID idResena,
        UUID idReserva,
        boolean visible,
        UUID idAdministradorModerador,
        String motivoOcultamiento,
        OffsetDateTime fechaOcultamiento,
        String mensaje) {
}
