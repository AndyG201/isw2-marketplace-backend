package co.edu.unbosque.marketplace.reserva.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ReservaConfirmacionDatos {
    UUID getIdAlojamiento();
    LocalDate getFechaEntrada();
    LocalDate getFechaSalida();
    BigDecimal getMontoTotal();
}
