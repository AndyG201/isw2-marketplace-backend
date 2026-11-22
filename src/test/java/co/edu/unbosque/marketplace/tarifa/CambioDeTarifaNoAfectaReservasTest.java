package co.edu.unbosque.marketplace.tarifa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reporte.application.IngresosAnfitrionService;
import co.edu.unbosque.marketplace.reporte.application.dto.IngresosAnfitrionResponse;
import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.tarifa.application.ConfiguracionTarifaService;
import co.edu.unbosque.marketplace.tarifa.application.dto.ConfigurarPorcentajesTarifaRequest;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

/**
 * Cuando el administrador cambia los porcentajes de la tarifa, lo que ya se cotizó, se reservó y se pagó no cambia:
 * cada cotización queda atada a la configuración con la que se hizo y esa configuración nunca se modifica.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CambioDeTarifaNoAfectaReservasTest {

    @Autowired private ConfiguracionTarifaService tarifas;
    @Autowired private IngresosAnfitrionService ingresos;
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService usuarios;

    @MockitoBean private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;
    private UUID idReserva;
    private UUID idConfiguracionNueva;

    @BeforeEach
    void pagarUnaReserva() throws Exception {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        idReserva = escenario.reservaConfirmada(30, 2);
    }

    @AfterEach
    void limpiar() {
        // La tarifa nueva apunta al administrador (un usuario del escenario): se borra primero.
        if (idConfiguracionNueva != null) {
            jdbc.update("DELETE FROM configuracion_tarifa WHERE id_configuracion = ?", idConfiguracionNueva);
        }
        escenario.limpiar();
    }

    private BigDecimal totalDeLaCotizacion() {
        return jdbc.queryForObject("""
                SELECT c.monto_total
                FROM vista_cotizacion c
                JOIN reserva r ON r.id_cotizacion = c.id_cotizacion
                WHERE r.id_reserva = ?
                """, BigDecimal.class, idReserva);
    }

    private BigDecimal montoDelPago() {
        return jdbc.queryForObject("SELECT monto FROM pago WHERE id_reserva = ?", BigDecimal.class, idReserva);
    }

    private BigDecimal porcentajeDeServicioDeLaConfiguracionAntigua() {
        return jdbc.queryForObject("SELECT porcentaje_servicio FROM configuracion_tarifa WHERE id_configuracion = ?",
                BigDecimal.class, escenario.idConfiguracion);
    }

    /** Lo mismo que hace el administrador al guardar nuevos porcentajes en la pestaña de tarifas. */
    private void cambiarLaTarifa(String servicio, String descuento) {
        idConfiguracionNueva = tarifas.configurar(escenario.idAnfitrion,
                new ConfigurarPorcentajesTarifaRequest(new BigDecimal(servicio), new BigDecimal(descuento)))
                .idConfiguracion();
    }

    @Test
    void laReservaYaPagadaConservaSuTotalSuPagoYSuConfiguracion() {
        BigDecimal totalAntes = totalDeLaCotizacion();
        BigDecimal pagoAntes = montoDelPago();
        assertEquals(0, new BigDecimal("220000").compareTo(totalAntes));
        assertEquals(0, totalAntes.compareTo(pagoAntes));

        cambiarLaTarifa("50.00", "20.00");

        assertEquals(0, totalAntes.compareTo(totalDeLaCotizacion()));
        assertEquals(0, pagoAntes.compareTo(montoDelPago()));
        assertEquals(0, new BigDecimal("10").compareTo(porcentajeDeServicioDeLaConfiguracionAntigua()));
    }

    @Test
    void elRepartoDelDineroDeEsaReservaSigueUsandoLosPorcentajesAntiguos() {
        IngresosAnfitrionResponse antes = ingresos.consultar(escenario.idAnfitrion);

        cambiarLaTarifa("50.00", "20.00");
        IngresosAnfitrionResponse despues = ingresos.consultar(escenario.idAnfitrion);

        assertEquals(antes.resumen(), despues.resumen());
        assertEquals(0, new BigDecimal("20000").compareTo(despues.resumen().comisionPlataforma()));
        assertEquals(0, new BigDecimal("200000").compareTo(despues.resumen().gananciasAnfitriones()));
    }

    @Test
    void unaCotizacionNuevaSiUsaLosPorcentajesNuevos() {
        cambiarLaTarifa("50.00", "20.00");
        // Una cotización de las mismas 2 noches, pero atada a la configuración nueva.
        UUID cotizacionNueva = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO cotizacion (id_cotizacion, id_publicacion, id_configuracion, fecha_entrada, fecha_salida,
                    cantidad_huespedes, precio_noche_aplicado, tarifa_limpieza, fecha_cotizacion)
                VALUES (?, ?, ?, current_date + 60, current_date + 62, 1, 100000, 0, now())
                """, cotizacionNueva, escenario.idPublicacion, idConfiguracionNueva);

        BigDecimal totalNuevo = jdbc.queryForObject("SELECT monto_total FROM vista_cotizacion WHERE id_cotizacion = ?",
                BigDecimal.class, cotizacionNueva);

        // 200.000 de noches + 50 % de servicio (100.000) - 20 % de descuento (40.000) = 260.000.
        assertEquals(0, new BigDecimal("260000").compareTo(totalNuevo));
        assertNotEquals(0, totalDeLaCotizacion().compareTo(totalNuevo));
        jdbc.update("DELETE FROM cotizacion WHERE id_cotizacion = ?", cotizacionNueva);
    }
}
