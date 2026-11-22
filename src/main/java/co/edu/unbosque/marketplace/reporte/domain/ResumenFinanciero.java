package co.edu.unbosque.marketplace.reporte.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Totales del dinero de un conjunto de reservas pagadas.
 *
 * @param pagadoPorHuespedes    suma de lo que pagaron los huéspedes
 * @param devoluciones          suma de lo que se les devolvió por cancelaciones
 * @param ingresosNetos         pagado menos devoluciones
 * @param comisionPlataforma    parte que se queda la plataforma (comisión menos descuento, después de devoluciones)
 * @param gananciasAnfitriones  parte que se quedan los anfitriones (después de devoluciones)
 * @param reservasConPago       cuántas reservas pagadas se sumaron
 */
public record ResumenFinanciero(
        BigDecimal pagadoPorHuespedes,
        BigDecimal devoluciones,
        BigDecimal ingresosNetos,
        BigDecimal comisionPlataforma,
        BigDecimal gananciasAnfitriones,
        long reservasConPago) {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    public static ResumenFinanciero vacio() {
        return de(List.of());
    }

    public static ResumenFinanciero de(List<RepartoReserva> repartos) {
        BigDecimal pagado = CERO;
        BigDecimal devoluciones = CERO;
        BigDecimal plataforma = CERO;
        BigDecimal anfitriones = CERO;
        for (RepartoReserva reparto : repartos) {
            pagado = pagado.add(reparto.pagado());
            devoluciones = devoluciones.add(reparto.devolucion());
            plataforma = plataforma.add(reparto.plataforma());
            anfitriones = anfitriones.add(reparto.anfitrion());
        }
        return new ResumenFinanciero(pagado, devoluciones, pagado.subtract(devoluciones), plataforma, anfitriones,
                repartos.size());
    }
}
