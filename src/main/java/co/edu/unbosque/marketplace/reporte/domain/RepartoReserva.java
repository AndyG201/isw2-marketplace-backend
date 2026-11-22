package co.edu.unbosque.marketplace.reporte.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cómo queda el dinero de una reserva pagada después de las devoluciones.
 *
 * @param pagado        lo que pagó el huésped
 * @param devolucion    lo que se le devolvió por cancelar (nunca más de lo pagado)
 * @param plataforma    parte que se queda la plataforma (comisión menos descuento, ya descontada la devolución)
 * @param anfitrion     parte que se queda el anfitrión (ya descontada la devolución)
 */
public record RepartoReserva(
        UUID idReserva,
        UUID idAnfitrion,
        UUID idPublicacion,
        String tituloPublicacion,
        BigDecimal pagado,
        BigDecimal devolucion,
        BigDecimal plataforma,
        BigDecimal anfitrion) {

    /** Lo que realmente se queda en el sistema: lo pagado menos lo devuelto. */
    public BigDecimal neto() {
        return pagado.subtract(devolucion);
    }
}
