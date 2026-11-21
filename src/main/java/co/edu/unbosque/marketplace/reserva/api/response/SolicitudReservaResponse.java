package co.edu.unbosque.marketplace.reserva.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SolicitudReservaResponse(
        UUID idReserva,
        UUID idCotizacion,
        String modoAceptacion,
        String estado,
        OffsetDateTime fechaSolicitud,
        OffsetDateTime retencionHasta) {
}