package co.edu.unbosque.marketplace.reserva.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReservaCancelacionInfo(
        UUID idReserva,
        UUID idHuesped,
        String estado,
        UUID idPoliticaAplicada,
        LocalDate fechaEntrada,
        BigDecimal montoTotal,
        BigDecimal montoPagado,
        String estadoPago) {
}
