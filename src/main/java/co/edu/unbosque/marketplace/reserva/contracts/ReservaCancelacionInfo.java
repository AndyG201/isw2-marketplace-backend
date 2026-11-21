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

    public ReservaCancelacionInfo conPago(BigDecimal montoDelPago, String estadoDelPago) {
        return new ReservaCancelacionInfo(idReserva, idHuesped, estado, idPoliticaAplicada, fechaEntrada,
                montoTotal, montoDelPago, estadoDelPago);
    }
}
