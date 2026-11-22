package co.edu.unbosque.marketplace.reporte.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
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
import co.edu.unbosque.marketplace.reporte.application.DashboardIndicadoresService;
import co.edu.unbosque.marketplace.reporte.application.IngresosAnfitrionService;
import co.edu.unbosque.marketplace.reporte.application.dto.IngresosAnfitrionResponse;
import co.edu.unbosque.marketplace.reporte.application.port.RepartoReservasRepository;
import co.edu.unbosque.marketplace.reporte.domain.PagoParaReparto;
import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

/**
 * Con una reserva pagada de verdad (2 noches a 100.000, comisión del 10 %, sin descuento ni limpieza: el huésped
 * paga 220.000), comprueba lo que sale de la base de datos y cómo se reparte el dinero.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RepartoReservasJdbcRepositoryTest {

    @Autowired private RepartoReservasRepository repartos;
    @Autowired private IngresosAnfitrionService ingresos;
    @Autowired private DashboardIndicadoresService dashboard;
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService usuarios;

    @MockitoBean private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;
    private UUID idReserva;

    @BeforeEach
    void pagarUnaReserva() throws Exception {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        idReserva = escenario.reservaConfirmada(30, 2);
    }

    @AfterEach
    void limpiar() {
        escenario.limpiar();
    }

    private PagoParaReparto delAnfitrion() {
        List<PagoParaReparto> pagos = repartos.pagosAprobadosDelAnfitrion(escenario.idAnfitrion);
        assertEquals(1, pagos.size());
        return pagos.get(0);
    }

    @Test
    void traeElPagoConLosDatosDeLaCotizacion() {
        PagoParaReparto pago = delAnfitrion();

        assertEquals(idReserva, pago.idReserva());
        assertEquals(escenario.idPublicacion, pago.idPublicacion());
        assertEquals("Loft", pago.tituloPublicacion());
        assertEquals(0, new BigDecimal("220000").compareTo(pago.montoPagado()));
        assertEquals(0, new BigDecimal("100000").compareTo(pago.precioPorNoche()));
        assertEquals(2, pago.noches());
        assertEquals(0, new BigDecimal("10").compareTo(pago.porcentajeServicio()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pago.porcentajeDescuento()));
        assertEquals(0, BigDecimal.ZERO.compareTo(pago.devolucion()));
    }

    @Test
    void otroAnfitrionNoVeLosPagosDeEste() {
        assertTrue(repartos.pagosAprobadosDelAnfitrion(UUID.randomUUID()).isEmpty());
        assertTrue(repartos.pagosAprobados().stream().anyMatch(pago -> idReserva.equals(pago.idReserva())));
    }

    @Test
    void incluyeLaDevolucionDeUnaCancelacion() {
        jdbc.update("""
                INSERT INTO cancelacion (id_cancelacion, id_reserva, id_usuario, motivo, valor_devolucion,
                    estado_devolucion, estado_final)
                VALUES (?, ?, ?, 'Cambio de planes', 110000, 'PROCESADA', 'CANCELADA')
                """, UUID.randomUUID(), idReserva, escenario.idHuesped);

        PagoParaReparto pago = delAnfitrion();

        assertEquals(0, new BigDecimal("110000").compareTo(pago.devolucion()));
    }

    @Test
    void ignoraLosPagosQueYaNoEstanPagados() {
        jdbc.update("UPDATE pago SET estado = 'REEMBOLSADO' WHERE id_reserva = ?", idReserva);

        assertTrue(repartos.pagosAprobadosDelAnfitrion(escenario.idAnfitrion).isEmpty());
    }

    @Test
    void elAnfitrionSeQuedaConElPrecioDeSusNochesYLaPlataformaConLaComision() {
        IngresosAnfitrionResponse respuesta = ingresos.consultar(escenario.idAnfitrion);

        assertEquals(0, new BigDecimal("220000").compareTo(respuesta.resumen().pagadoPorHuespedes()));
        assertEquals(0, new BigDecimal("20000").compareTo(respuesta.resumen().comisionPlataforma()));
        assertEquals(0, new BigDecimal("200000").compareTo(respuesta.resumen().gananciasAnfitriones()));
        assertEquals(1, respuesta.publicaciones().size());
        assertEquals("Loft", respuesta.publicaciones().get(0).titulo());
    }

    @Test
    void siSeCancelaConLaMitadDeDevolucionCadaUnoPierdeLaMitad() {
        jdbc.update("""
                INSERT INTO cancelacion (id_cancelacion, id_reserva, id_usuario, motivo, valor_devolucion,
                    estado_devolucion, estado_final)
                VALUES (?, ?, ?, 'Cambio de planes', 110000, 'PROCESADA', 'CANCELADA')
                """, UUID.randomUUID(), idReserva, escenario.idHuesped);

        IngresosAnfitrionResponse respuesta = ingresos.consultar(escenario.idAnfitrion);

        assertEquals(0, new BigDecimal("110000").compareTo(respuesta.resumen().devoluciones()));
        assertEquals(0, new BigDecimal("110000").compareTo(respuesta.resumen().ingresosNetos()));
        assertEquals(0, new BigDecimal("10000").compareTo(respuesta.resumen().comisionPlataforma()));
        assertEquals(0, new BigDecimal("100000").compareTo(respuesta.resumen().gananciasAnfitriones()));
    }

    @Test
    void elTableroDelAdministradorTraeLasFinanzasDeLaPlataforma() {
        var finanzas = dashboard.consultar().finanzas();

        assertTrue(finanzas.reservasConPago() >= 1);
        assertTrue(finanzas.pagadoPorHuespedes().compareTo(new BigDecimal("220000")) >= 0);
        assertEquals(0, finanzas.ingresosNetos()
                .compareTo(finanzas.comisionPlataforma().add(finanzas.gananciasAnfitriones())));
    }

    @Test
    void elEndpointDelAnfitrionDevuelveSusIngresosYSoloLosSuyos() throws Exception {
        mockMvc.perform(get("/anfitrion/ingresos").header("Authorization", escenario.token(escenario.idAnfitrion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.reservasConPago").value(1))
                .andExpect(jsonPath("$.resumen.gananciasAnfitriones").value(200000.0))
                .andExpect(jsonPath("$.resumen.comisionPlataforma").value(20000.0))
                .andExpect(jsonPath("$.publicaciones[0].titulo").value("Loft"));
    }

    @Test
    void unHuespedNoPuedeConsultarIngresosDeAnfitrion() throws Exception {
        mockMvc.perform(get("/anfitrion/ingresos").header("Authorization", escenario.token(escenario.idHuesped)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinSesionSeRechaza() throws Exception {
        mockMvc.perform(get("/anfitrion/ingresos")).andExpect(status().isUnauthorized());
    }
}
