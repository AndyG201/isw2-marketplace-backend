package co.edu.unbosque.marketplace.disponibilidad.contracts;

import java.time.LocalDate;
import java.util.UUID;

public interface CotizacionDisponibilidadQuery {

    boolean estaDisponible(
            UUID idAlojamiento,
            LocalDate fechaEntrada,
            LocalDate fechaSalida
    );
}