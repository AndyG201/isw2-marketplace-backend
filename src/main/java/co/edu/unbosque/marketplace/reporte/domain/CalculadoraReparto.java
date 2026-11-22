package co.edu.unbosque.marketplace.reporte.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Reparte lo que pagó un huésped entre la plataforma y el anfitrión.
 *
 * <p>La cotización suma: noches + limpieza + comisión de la plataforma + servicios adicionales - descuento. La comisión
 * y el descuento son porcentajes del valor de las noches. Se asume que:
 * <ul>
 *   <li>La plataforma se queda con {@code comisión - descuento} (el descuento lo asume ella).</li>
 *   <li>El anfitrión se queda con el resto: noches + limpieza + servicios adicionales.</li>
 *   <li>Si la reserva se cancela, la devolución sale de ambos en la misma proporción en que cobraron.</li>
 * </ul>
 * Los redondeos son los de la cotización (dos decimales, hacia arriba en la mitad), para que los números cuadren.
 */
public final class CalculadoraReparto {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private CalculadoraReparto() {
    }

    public static RepartoReserva repartir(PagoParaReparto pago) {
        BigDecimal pagado = moneda(pago.montoPagado());
        BigDecimal devolucion = devolucionAcotada(pago.devolucion(), pagado);

        BigDecimal valorNoches = moneda(pago.precioPorNoche().multiply(BigDecimal.valueOf(pago.noches())));
        BigDecimal comision = porcentaje(valorNoches, pago.porcentajeServicio());
        BigDecimal descuento = porcentaje(valorNoches, pago.porcentajeDescuento());
        BigDecimal plataformaBruta = comision.subtract(descuento);

        BigDecimal plataforma = plataformaBruta;
        if (devolucion.signum() > 0) {
            // Cada uno conserva la fracción de lo pagado que no se devolvió.
            plataforma = moneda(plataformaBruta.multiply(pagado.subtract(devolucion))
                    .divide(pagado, 2, RoundingMode.HALF_UP));
        }
        BigDecimal anfitrion = pagado.subtract(devolucion).subtract(plataforma);

        return new RepartoReserva(pago.idReserva(), pago.idAnfitrion(), pago.idPublicacion(),
                pago.tituloPublicacion(), pagado, devolucion, plataforma, anfitrion);
    }

    private static BigDecimal devolucionAcotada(BigDecimal devolucion, BigDecimal pagado) {
        if (devolucion == null || devolucion.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return moneda(devolucion).min(pagado);
    }

    private static BigDecimal porcentaje(BigDecimal base, BigDecimal porcentaje) {
        return moneda(base.multiply(porcentaje).divide(CIEN, 2, RoundingMode.HALF_UP));
    }

    private static BigDecimal moneda(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
