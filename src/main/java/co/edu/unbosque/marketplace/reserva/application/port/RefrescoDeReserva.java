package co.edu.unbosque.marketplace.reserva.application.port;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;

/**
 * Vuelve a leer una reserva desde la base de datos. Se usa cuando entre la lectura original y la decisión pasó
 * tiempo (por ejemplo, la llamada a Stripe) y otro proceso pudo cambiar su estado.
 */
public interface RefrescoDeReserva {

    void refrescar(Reserva reserva);
}
