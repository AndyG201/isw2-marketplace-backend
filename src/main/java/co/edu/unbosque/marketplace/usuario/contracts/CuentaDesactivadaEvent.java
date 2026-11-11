package co.edu.unbosque.marketplace.usuario.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CuentaDesactivadaEvent(
        UUID idUsuario,
        OffsetDateTime fechaEvento
) {
}
