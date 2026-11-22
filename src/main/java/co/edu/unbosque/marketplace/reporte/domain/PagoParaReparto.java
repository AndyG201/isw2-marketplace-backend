package co.edu.unbosque.marketplace.reporte.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Un pago aprobado con los datos necesarios para repartirlo entre la plataforma y el anfitrión: lo que pagó el huésped,
 * cómo se compuso la cotización (precio por noche, noches y porcentajes de la tarifa vigente cuando se cotizó) y lo que
 * se le devolvió si la reserva se canceló.
 */
public record PagoParaReparto(
        UUID idReserva,
        UUID idAnfitrion,
        UUID idPublicacion,
        String tituloPublicacion,
        BigDecimal montoPagado,
        BigDecimal precioPorNoche,
        long noches,
        BigDecimal porcentajeServicio,
        BigDecimal porcentajeDescuento,
        BigDecimal devolucion) {
}
