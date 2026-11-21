package co.edu.unbosque.marketplace.reserva.infrastructure.persistence;

import java.time.LocalDate;
import java.util.UUID;

public interface ReservaSolicitudDatos {

    UUID getIdCotizacion();

    UUID getIdPublicacion();

    UUID getIdAlojamiento();

    UUID getIdPolitica();

    UUID getIdAnfitrion();

    String getModoAceptacion();

    String getEstadoPublicacion();

    int getCapacidad();

    LocalDate getFechaEntrada();

    LocalDate getFechaSalida();

    int getCantidadHuespedes();
}