package co.edu.unbosque.marketplace.reporte.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CalculadoraRepartoTest {

    private static final UUID RESERVA = UUID.randomUUID();
    private static final UUID ANFITRION = UUID.randomUUID();
    private static final UUID PUBLICACION = UUID.randomUUID();

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    /**
     * 2 noches a 100.000: noches 200.000, comisión del 10 % (20.000), descuento del 5 % (10.000), limpieza 30.000 y un
     * servicio adicional de 20.000. El huésped paga 200.000 + 30.000 + 20.000 + 20.000 - 10.000 = 260.000.
     */
    private static PagoParaReparto pago(String devolucion) {
        return new PagoParaReparto(RESERVA, ANFITRION, PUBLICACION, "Loft", d("260000.00"), d("100000.00"), 2,
                d("10.00"), d("5.00"), d(devolucion));
    }

    @Test
    void sinCancelacionLaPlataformaSeQuedaConLaComisionMenosElDescuentoYElAnfitrionConElResto() {
        RepartoReserva reparto = CalculadoraReparto.repartir(pago("0"));

        assertEquals(d("260000.00"), reparto.pagado());
        assertEquals(d("0.00"), reparto.devolucion());
        assertEquals(d("10000.00"), reparto.plataforma());
        assertEquals(d("250000.00"), reparto.anfitrion());
        assertEquals(d("260000.00"), reparto.neto());
    }

    @Test
    void conDevolucionParcialAmbosPierdenEnLaMismaProporcion() {
        // La política devuelve el 50 %: se devuelven 130.000 y cada uno conserva la mitad de lo suyo.
        RepartoReserva reparto = CalculadoraReparto.repartir(pago("130000.00"));

        assertEquals(d("130000.00"), reparto.devolucion());
        assertEquals(d("130000.00"), reparto.neto());
        assertEquals(d("5000.00"), reparto.plataforma());
        assertEquals(d("125000.00"), reparto.anfitrion());
    }

    @Test
    void conDevolucionTotalNadieSeQuedaConNada() {
        RepartoReserva reparto = CalculadoraReparto.repartir(pago("260000.00"));

        assertEquals(d("0.00"), reparto.neto());
        assertEquals(d("0.00"), reparto.plataforma());
        assertEquals(d("0.00"), reparto.anfitrion());
    }

    @Test
    void unaDevolucionMayorQueLoPagadoSeLimitaALoPagado() {
        RepartoReserva reparto = CalculadoraReparto.repartir(pago("999999.00"));

        assertEquals(d("260000.00"), reparto.devolucion());
        assertEquals(d("0.00"), reparto.neto());
    }

    @Test
    void sinDevolucionNulaONegativaSeTrataComoCero() {
        PagoParaReparto sinDato = new PagoParaReparto(RESERVA, ANFITRION, PUBLICACION, "Loft", d("260000.00"),
                d("100000.00"), 2, d("10.00"), d("5.00"), null);
        PagoParaReparto negativa = pago("-5");

        assertEquals(d("0.00"), CalculadoraReparto.repartir(sinDato).devolucion());
        assertEquals(d("0.00"), CalculadoraReparto.repartir(negativa).devolucion());
        assertEquals(d("10000.00"), CalculadoraReparto.repartir(sinDato).plataforma());
    }

    @Test
    void siElDescuentoEsMayorQueLaComisionLaPlataformaPonePlataYElAnfitrionCobraSuPrecioCompleto() {
        // Comisión 5 % (10.000) y descuento 10 % (20.000): la plataforma asume la diferencia.
        PagoParaReparto pago = new PagoParaReparto(RESERVA, ANFITRION, PUBLICACION, "Loft", d("240000.00"),
                d("100000.00"), 2, d("5.00"), d("10.00"), d("0"));

        RepartoReserva reparto = CalculadoraReparto.repartir(pago);

        assertEquals(d("-10000.00"), reparto.plataforma());
        assertEquals(d("250000.00"), reparto.anfitrion());
        assertEquals(reparto.neto(), reparto.plataforma().add(reparto.anfitrion()));
    }

    @Test
    void sinComisionNiDescuentoTodoEsDelAnfitrion() {
        PagoParaReparto pago = new PagoParaReparto(RESERVA, ANFITRION, PUBLICACION, "Loft", d("230000.00"),
                d("100000.00"), 2, d("0.00"), d("0.00"), d("0"));

        RepartoReserva reparto = CalculadoraReparto.repartir(pago);

        assertEquals(d("0.00"), reparto.plataforma());
        assertEquals(d("230000.00"), reparto.anfitrion());
    }

    @Test
    void conCentavosLosRedondeosSiempreCuadranConLoNeto() {
        for (String devolucion : List.of("0", "1.01", "33333.33", "77777.77", "100000.00")) {
            PagoParaReparto pago = new PagoParaReparto(RESERVA, ANFITRION, PUBLICACION, "Loft", d("123456.79"),
                    d("33333.33"), 3, d("7.50"), d("2.25"), d(devolucion));

            RepartoReserva reparto = CalculadoraReparto.repartir(pago);

            assertEquals(reparto.neto(), reparto.plataforma().add(reparto.anfitrion()), "devolución " + devolucion);
            assertEquals(2, reparto.plataforma().scale());
            assertEquals(2, reparto.anfitrion().scale());
        }
    }

    @Test
    void elResumenSumaPagadoDevueltoYReparto() {
        RepartoReserva sinCancelar = CalculadoraReparto.repartir(pago("0"));
        RepartoReserva cancelada = CalculadoraReparto.repartir(pago("130000.00"));

        ResumenFinanciero resumen = ResumenFinanciero.de(List.of(sinCancelar, cancelada));

        assertEquals(d("520000.00"), resumen.pagadoPorHuespedes());
        assertEquals(d("130000.00"), resumen.devoluciones());
        assertEquals(d("390000.00"), resumen.ingresosNetos());
        assertEquals(d("15000.00"), resumen.comisionPlataforma());
        assertEquals(d("375000.00"), resumen.gananciasAnfitriones());
        assertEquals(2, resumen.reservasConPago());
        assertEquals(resumen.ingresosNetos(), resumen.comisionPlataforma().add(resumen.gananciasAnfitriones()));
    }

    @Test
    void sinReservasPagadasTodoQuedaEnCero() {
        ResumenFinanciero resumen = ResumenFinanciero.vacio();

        assertEquals(d("0.00"), resumen.pagadoPorHuespedes());
        assertEquals(d("0.00"), resumen.ingresosNetos());
        assertEquals(d("0.00"), resumen.comisionPlataforma());
        assertEquals(0, resumen.reservasConPago());
    }
}
