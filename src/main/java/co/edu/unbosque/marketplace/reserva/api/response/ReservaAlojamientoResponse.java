package co.edu.unbosque.marketplace.reserva.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReservaAlojamientoResponse(
        UUID idReserva,
        UUID idAlojamiento,
        String alojamiento,
        UUID idHuesped,
        String huesped,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        String estado,
        BigDecimal montoTotal) {
}
