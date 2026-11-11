package co.edu.unbosque.marketplace.publicacion.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PublicacionDesbloqueadaEvent(
        UUID idPublicacion,
        UUID idAnfitrion,
        String titulo,
        String motivo,
        OffsetDateTime fechaEvento
) {
}
