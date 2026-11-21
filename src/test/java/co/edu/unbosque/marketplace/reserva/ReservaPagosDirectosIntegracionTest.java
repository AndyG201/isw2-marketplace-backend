package co.edu.unbosque.marketplace.reserva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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
import co.edu.unbosque.marketplace.reserva.application.ReservaConfirmacionService;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoAprobadoService;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoConciliacionWorker;
import co.edu.unbosque.marketplace.reserva.application.dto.ConfirmacionReservaResponse;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaPagosDirectosIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @Autowired
    private ReservaConfirmacionService confirmaciones;

    @Autowired
    private ReservaPagoAprobadoService pagosAprobados;

    @Autowired
    private ReservaPagoConciliacionWorker conciliacion;

    @MockitoBean
    private PagoSandboxGateway pasarela;

    private final List<EscenarioReservaPrueba> escenarios = new ArrayList<>();

    @AfterEach
    void limpiar() {
        for (int i = escenarios.size() - 1; i >= 0; i--) {
            escenarios.get(i).limpiar();
        }
    }

    @Test
    void laConfirmacionInmediataCobraUnaVezYConfirmaLaReserva() throws Exception {
        pasarelaResponde(true);
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID idReserva = reservaRetenida(escenario);
        String clave = "inmediata-" + idReserva;

        ConfirmacionReservaResponse primera = confirmaciones.confirmar(escenario.idHuesped, idReserva, clave);
        ConfirmacionReservaResponse repetida = confirmaciones.confirmar(escenario.idHuesped, idReserva, clave);

        assertEquals("CONFIRMADA", primera.estadoReserva());
        assertEquals("PAGADO", primera.estadoPago());
        assertEquals("pi_directo", primera.referenciaPago());
        assertEquals("PAGADO", repetida.estadoPago());
        assertEquals("Resultado de pago idempotente.", repetida.mensaje());
        verify(pasarela, times(1)).procesar(any(), any());
    }

    @Test
    void siLaConfirmacionInmediataEsRechazadaSeLiberanLasNoches() throws Exception {
        pasarelaResponde(false);
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID idReserva = reservaRetenida(escenario);

        ConfirmacionReservaResponse respuesta =
                confirmaciones.confirmar(escenario.idHuesped, idReserva, "rechazo-" + idReserva);

        assertEquals("FALLIDO", respuesta.estadoPago());
        assertEquals("PENDIENTE_PAGO", respuesta.estadoReserva());
        assertNull(respuesta.retencionHasta());
        assertEquals(0, escenario.nochesOcupadas());
    }

    @Test
    void elPagoDeUnaSolicitudAprobadaEsIdempotenteYNoSeCobraDosVeces() throws Exception {
        pasarelaResponde(true);
        EscenarioReservaPrueba escenario = escenario("APROBACION");
        UUID idReserva = solicitudAprobada(escenario);
        String clave = "aprobada-" + idReserva;

        ConfirmacionReservaResponse pago = pagosAprobados.pagar(escenario.idHuesped, idReserva, clave);
        ConfirmacionReservaResponse repetido = pagosAprobados.pagar(escenario.idHuesped, idReserva, clave);
        ConfirmacionReservaResponse otraClave =
                pagosAprobados.pagar(escenario.idHuesped, idReserva, "otra-" + idReserva);

        assertEquals("CONFIRMADA", pago.estadoReserva());
        assertEquals("PAGADO", pago.estadoPago());
        assertEquals("Resultado de pago idempotente.", repetido.mensaje());
        assertEquals("La reserva ya fue pagada.", otraClave.mensaje());
        verify(pasarela, times(1)).procesar(any(), any());
    }

    @Test
    void siElPagoDeUnaSolicitudAprobadaEsRechazadoSeLiberanLasNoches() throws Exception {
        pasarelaResponde(false);
        EscenarioReservaPrueba escenario = escenario("APROBACION");
        UUID idReserva = solicitudAprobada(escenario);

        ConfirmacionReservaResponse respuesta =
                pagosAprobados.pagar(escenario.idHuesped, idReserva, "rechazo-" + idReserva);

        assertEquals("FALLIDO", respuesta.estadoPago());
        assertEquals(0, escenario.nochesOcupadas());
    }

    @Test
    void laConciliacionRegistraUnRechazoDeStripe() throws Exception {
        when(pasarela.procesar(any(), any()))
                .thenThrow(new IllegalStateException("Stripe no respondió"))
                .thenReturn(new ResultadoPagoSandbox(false, " ", "Tarjeta rechazada"));
        EscenarioReservaPrueba escenario = escenario("INMEDIATA");
        UUID idReserva = reservaRetenida(escenario);
        escenario.pagar(escenario.idHuesped, idReserva, "conciliacion-" + idReserva);

        escenario.adelantarConciliacion(idReserva);
        conciliacion.conciliarPendientes();

        assertEquals("FALLIDO", jdbc.queryForObject(
                "SELECT estado FROM pago WHERE id_reserva = ?", String.class, idReserva));
        assertEquals("Tarjeta rechazada", jdbc.queryForObject(
                "SELECT ultimo_error FROM operacion_pendiente WHERE id_reserva = ?", String.class, idReserva));
        assertEquals("PENDIENTE_PAGO", escenario.estadoReserva(idReserva));
    }

    private EscenarioReservaPrueba escenario(String modoAceptacion) {
        EscenarioReservaPrueba escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, modoAceptacion, "FLEXIBLE", 100, 24);
        escenarios.add(escenario);
        return escenario;
    }

    private UUID reservaRetenida(EscenarioReservaPrueba escenario) throws Exception {
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        escenario.retener(escenario.idHuesped, idReserva);
        return idReserva;
    }

    private UUID solicitudAprobada(EscenarioReservaPrueba escenario) throws Exception {
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        mockMvc.perform(patch("/reservas/{id}/aprobar", idReserva)
                        .header("Authorization", escenario.token(escenario.idAnfitrion)))
                .andReturn();
        return idReserva;
    }

    private void pasarelaResponde(boolean aprobado) {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(
                aprobado, "pi_directo", aprobado ? "Pago aprobado" : "Tarjeta rechazada"));
    }
}
