package co.edu.unbosque.marketplace.reserva.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaHistorialDatos(
        UUID idReserva,
        UUID idAlojamiento,
        String alojamiento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        BigDecimal montoTotal,
        String estado,
        OffsetDateTime fechaSolicitud,
        OffsetDateTime retencionHasta,
        boolean resenada) {
}