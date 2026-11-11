package co.edu.unbosque.marketplace.usuario.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CuentaRestringidaEvent(
        UUID idUsuario,
        OffsetDateTime fechaEvento
) {
}
