package co.edu.unbosque.marketplace.cancelacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoIntentoService;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CancelacionReservaIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @Autowired
    private ReservaPagoIntentoService intentosPago;

    @MockitoBean
    private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;

    @BeforeEach
    void pasarelaAprueba() {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
    }

    @AfterEach
    void limpiar() {
        if (escenario != null) {
            escenario.limpiar();
        }
    }

    @Test
    void laPoliticaFlexibleDevuelveTodoSiSeCancelaConAnticipacion() throws Exception {
        escenario = escenario("FLEXIBLE", 100, 24);
        UUID idReserva = escenario.reservaConfirmada(30, 2);

        MvcResult cancelacion = cancelar(escenario.idHuesped, idReserva);

        assertEquals(200, cancelacion.getResponse().getStatus());
        assertEquals("CANCELADA", campo(cancelacion, "$.estadoFinal"));
        assertEquals("PROCESADA", campo(cancelacion, "$.estadoDevolucion"));
        assertMonto("220000.00", campo(cancelacion, "$.valorDevolucion"));
        assertMonto("100", campo(cancelacion, "$.porcentajeDevolucion"));
        assertEquals("CANCELADA", escenario.estadoReserva(idReserva));
        assertEquals(0, escenario.nochesOcupadas());
    }

    @Test
    void laPoliticaModeradaDevuelveSuPorcentajeSiSeCancelaConAnticipacion() throws Exception {
        escenario = escenario("MODERADA", 50, 120);
        UUID idReserva = escenario.reservaConfirmada(30, 2);

        MvcResult cancelacion = cancelar(escenario.idHuesped, idReserva);

        assertEquals("PROCESADA", campo(cancelacion, "$.estadoDevolucion"));
        assertMonto("110000.00", campo(cancelacion, "$.valorDevolucion"));
    }

    @Test
    void laPoliticaModeradaNoDevuelveSiFaltaAnticipacion() throws Exception {
        escenario = escenario("MODERADA", 50, 120);
        UUID idReserva = escenario.reservaConfirmada(3, 2);

        MvcResult cancelacion = cancelar(escenario.idHuesped, idReserva);

        assertEquals("NO_APLICA", campo(cancelacion, "$.estadoDevolucion"));
        assertMonto("0", campo(cancelacion, "$.valorDevolucion"));
        assertEquals("CANCELADA", escenario.estadoReserva(idReserva));
    }

    @Test
    void laPoliticaEstrictaNoDevuelveNada() throws Exception {
        escenario = escenario("ESTRICTA", 0, null);
        UUID idReserva = escenario.reservaConfirmada(30, 2);

        MvcResult cancelacion = cancelar(escenario.idHuesped, idReserva);

        assertEquals("NO_APLICA", campo(cancelacion, "$.estadoDevolucion"));
        assertMonto("0", campo(cancelacion, "$.valorDevolucion"));
        assertEquals(0, escenario.nochesOcupadas());
    }

    @Test
    void cancelarUnaReservaSinPagarNoGeneraDevolucionYLiberaLasNoches() throws Exception {
        escenario = escenario("FLEXIBLE", 100, 24);
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.retener(escenario.idHuesped, idReserva);

        MvcResult cancelacion = cancelar(escenario.idHuesped, idReserva);

        assertEquals(200, cancelacion.getResponse().getStatus());
        assertEquals("NO_APLICA", campo(cancelacion, "$.estadoDevolucion"));
        assertEquals("CANCELADA", escenario.estadoReserva(idReserva));
        assertEquals(0, escenario.nochesOcupadas());
    }

    @Test
    void cancelarDosVecesDevuelveElMismoRegistro() throws Exception {
        escenario = escenario("FLEXIBLE", 100, 24);
        UUID idReserva = escenario.reservaConfirmada(30, 2);

        MvcResult primera = cancelar(escenario.idHuesped, idReserva);
        MvcResult segunda = cancelar(escenario.idHuesped, idReserva);

        assertEquals(200, segunda.getResponse().getStatus());
        assertEquals((String) campo(primera, "$.idCancelacion"), campo(segunda, "$.idCancelacion"));
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM cancelacion WHERE id_reserva = ?", Integer.class, idReserva));
    }

    @Test
    void noSePuedeCancelarMientrasElPagoSigaPendiente() throws Exception {
        escenario = escenario("FLEXIBLE", 100, 24);
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.retener(escenario.idHuesped, idReserva);
        intentosPago.preparar(escenario.idHuesped, idReserva, "pago-pendiente-" + idReserva);

        assertEquals(409, cancelar(escenario.idHuesped, idReserva).getResponse().getStatus());
        assertEquals("PENDIENTE_PAGO", escenario.estadoReserva(idReserva));
    }

    @Test
    void otroHuespedNoPuedeCancelarUnaReservaAjena() throws Exception {
        escenario = escenario("FLEXIBLE", 100, 24);
        UUID idReserva = escenario.reservaConfirmada(30, 2);

        assertEquals(403, cancelar(escenario.idOtroHuesped, idReserva).getResponse().getStatus());
        assertEquals("CONFIRMADA", escenario.estadoReserva(idReserva));
        assertEquals(2, escenario.nochesOcupadas());
    }

    private EscenarioReservaPrueba escenario(String politica, int porcentaje, Integer anticipacionHoras) {
        return EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", politica, porcentaje, anticipacionHoras);
    }

    private MvcResult cancelar(UUID idUsuario, UUID idReserva) throws Exception {
        return mockMvc.perform(post("/reservas/{id}/cancelacion", idReserva)
                        .header("Authorization", escenario.token(idUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Cambio de planes\"}"))
                .andReturn();
    }

    private static void assertMonto(String esperado, Object valor) {
        assertEquals(0, new BigDecimal(esperado).compareTo(new BigDecimal(valor.toString())),
                "Se esperaba " + esperado + " pero llegó " + valor);
    }

    private static <T> T campo(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), ruta);
    }
}
