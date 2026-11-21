package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReservaAnfitrionDatos(
        UUID idReserva,
        UUID idAlojamiento,
        String alojamiento,
        UUID idHuesped,
        String huesped,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        BigDecimal montoTotal,
        String estado,
        OffsetDateTime retencionHasta) {
}
