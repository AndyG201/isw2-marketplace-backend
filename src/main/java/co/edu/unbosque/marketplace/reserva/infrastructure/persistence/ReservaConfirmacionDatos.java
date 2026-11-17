package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ReservaConfirmacionDatos {
    UUID getIdAlojamiento();
    LocalDate getFechaEntrada();
    LocalDate getFechaSalida();
    BigDecimal getMontoTotal();
}
