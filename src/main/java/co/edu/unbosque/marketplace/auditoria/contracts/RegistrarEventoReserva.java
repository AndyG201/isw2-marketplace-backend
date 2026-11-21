package co.edu.unbosque.marketplace.auditoria.contracts;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegistrarEventoReserva(UUID idUsuario, UUID idReserva, OffsetDateTime fechaEvento,
        String accion, String resultado, String detalle) {
}
