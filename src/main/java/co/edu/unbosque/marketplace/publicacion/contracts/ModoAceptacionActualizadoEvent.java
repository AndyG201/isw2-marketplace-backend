package co.edu.unbosque.marketplace.publicacion.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ModoAceptacionActualizadoEvent(
        UUID idPublicacion,
        UUID idAnfitrion,
        String titulo,
        String modoAnterior,
        String modoNuevo,
        OffsetDateTime fechaEvento
) {
}
