package co.edu.unbosque.marketplace.reserva.application.port;

/**
 * Candado de corta duración para serializar operaciones que comparten la misma
 * clave de idempotencia. El candado dura lo que dure la transacción en curso y
 * se libera solo al confirmarla o deshacerla.
 */
public interface BloqueoClaveIdempotencia {

    /**
     * Espera hasta obtener el candado de la clave dentro del ámbito indicado.
     * Debe llamarse dentro de una transacción.
     */
    void bloquear(String ambito, String clave);
}
