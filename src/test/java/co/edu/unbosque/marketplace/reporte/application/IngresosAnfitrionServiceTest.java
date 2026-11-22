package co.edu.unbosque.marketplace.reporte.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.reporte.application.dto.IngresosAnfitrionResponse;
import co.edu.unbosque.marketplace.reporte.application.port.RepartoReservasRepository;
import co.edu.unbosque.marketplace.reporte.domain.PagoParaReparto;

class IngresosAnfitrionServiceTest {

    private final UUID anfitrion = UUID.randomUUID();
    private final UUID loft = UUID.randomUUID();
    private final UUID cabana = UUID.randomUUID();

    private final RepartoReservasRepository repartos = mock(RepartoReservasRepository.class);
    private final IngresosAnfitrionService servicio = new IngresosAnfitrionService(repartos);

    private PagoParaReparto pago(UUID publicacion, String titulo, String pagado, String devolucion) {
        return new PagoParaReparto(UUID.randomUUID(), anfitrion, publicacion, titulo, new BigDecimal(pagado),
                new BigDecimal("100000"), 2, new BigDecimal("10"), new BigDecimal("0"), new BigDecimal(devolucion));
    }

    @Test
    void sinSesionPideIniciarSesionYNoConsultaNada() {
        assertThrows(SesionRequeridaException.class, () -> servicio.consultar(null));
        verify(repartos, never()).pagosAprobadosDelAnfitrion(null);
    }

    @Test
    void sinPagosDevuelveTodoEnCeroYSinPublicaciones() {
        when(repartos.pagosAprobadosDelAnfitrion(anfitrion)).thenReturn(List.of());

        IngresosAnfitrionResponse respuesta = servicio.consultar(anfitrion);

        assertEquals(0, respuesta.resumen().reservasConPago());
        assertEquals(new BigDecimal("0.00"), respuesta.resumen().gananciasAnfitriones());
        assertTrue(respuesta.publicaciones().isEmpty());
    }

    @Test
    void sumaLasGananciasDelAnfitrionDescontandoComisionYDevoluciones() {
        // 220.000 pagados: comisión 20.000, el anfitrión se queda 200.000. La segunda reserva se cancela con 50 %.
        when(repartos.pagosAprobadosDelAnfitrion(anfitrion)).thenReturn(List.of(
                pago(loft, "Loft", "220000", "0"),
                pago(loft, "Loft", "220000", "110000")));

        IngresosAnfitrionResponse respuesta = servicio.consultar(anfitrion);

        assertEquals(new BigDecimal("440000.00"), respuesta.resumen().pagadoPorHuespedes());
        assertEquals(new BigDecimal("110000.00"), respuesta.resumen().devoluciones());
        assertEquals(new BigDecimal("30000.00"), respuesta.resumen().comisionPlataforma());
        assertEquals(new BigDecimal("300000.00"), respuesta.resumen().gananciasAnfitriones());
        assertEquals(2, respuesta.resumen().reservasConPago());
    }

    @Test
    void desglosaPorPublicacionYPoneArribaLaQueMasGenero() {
        when(repartos.pagosAprobadosDelAnfitrion(anfitrion)).thenReturn(List.of(
                pago(loft, "Loft", "220000", "0"),
                pago(cabana, "Cabaña", "220000", "0"),
                pago(cabana, "Cabaña", "220000", "0")));

        IngresosAnfitrionResponse respuesta = servicio.consultar(anfitrion);

        List<IngresosAnfitrionResponse.IngresoPublicacion> publicaciones = respuesta.publicaciones();
        assertEquals(2, publicaciones.size());
        assertEquals("Cabaña", publicaciones.get(0).titulo());
        assertEquals(2, publicaciones.get(0).reservasConPago());
        assertEquals(new BigDecimal("400000.00"), publicaciones.get(0).ganancias());
        assertEquals("Loft", publicaciones.get(1).titulo());
        assertEquals(new BigDecimal("200000.00"), publicaciones.get(1).ganancias());
    }

    @Test
    void conIgualGananciaSeOrdenaPorTitulo() {
        when(repartos.pagosAprobadosDelAnfitrion(anfitrion)).thenReturn(List.of(
                pago(loft, "Loft", "220000", "0"),
                pago(cabana, "Cabaña", "220000", "0")));

        IngresosAnfitrionResponse respuesta = servicio.consultar(anfitrion);

        assertEquals(List.of("Cabaña", "Loft"),
                respuesta.publicaciones().stream().map(IngresosAnfitrionResponse.IngresoPublicacion::titulo).toList());
    }
}
