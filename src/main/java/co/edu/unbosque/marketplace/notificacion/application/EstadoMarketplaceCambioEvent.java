package co.edu.unbosque.marketplace.notificacion.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EstadoMarketplaceCambioEvent(
        String entidad,
        UUID idEntidad,
        String estadoAnterior,
        String estadoNuevo,
        OffsetDateTime fechaEvento) {
}
