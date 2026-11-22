package co.edu.unbosque.marketplace.reserva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.infrastructure.gateway.PagoSimuladoGateway;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

/**
 * Con el perfil {@code pago-simulado} la aplicación completa paga una reserva sin hablar con Stripe: no hay clave
 * configurada y no se hace ninguna llamada de red.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "pago-simulado"})
class ReservaPagoSimuladoSinStripeIntegracionTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService usuarios;
    @Autowired private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;

    @AfterEach
    void limpiar() {
        if (escenario != null) {
            escenario.limpiar();
        }
    }

    @Test
    void conElPerfilLaPasarelaActivaEsLaSimulada() {
        assertInstanceOf(PagoSimuladoGateway.class, pasarela);
    }

    @Test
    void unaReservaInmediataSePagaYQuedaConfirmadaSinStripe() throws Exception {
        escenario = EscenarioReservaPrueba.crear(jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        UUID idReserva = escenario.solicitarReserva(escenario.idHuesped, escenario.nuevaCotizacion(30, 2));
        assertEquals(200, escenario.retener(escenario.idHuesped, idReserva).getResponse().getStatus());

        String clave = "pago-simulado-" + idReserva;
        MvcResult pago = escenario.pagar(escenario.idHuesped, idReserva, clave);
        MvcResult repetido = escenario.pagar(escenario.idHuesped, idReserva, clave);

        assertEquals(200, pago.getResponse().getStatus());
        assertEquals("CONFIRMADA", JsonPath.read(pago.getResponse().getContentAsString(), "$.estadoReserva"));
        assertEquals("PAGADO", JsonPath.read(pago.getResponse().getContentAsString(), "$.estadoPago"));
        String referencia = JsonPath.read(pago.getResponse().getContentAsString(), "$.referenciaPago");
        assertTrue(referencia.startsWith("pi_sim"), referencia);
        assertEquals("CONFIRMADA", escenario.estadoReserva(idReserva));

        // Repetir la clave no cobra dos veces: mismo pago, misma referencia.
        assertEquals(200, repetido.getResponse().getStatus());
        assertEquals(referencia, JsonPath.read(repetido.getResponse().getContentAsString(), "$.referenciaPago"));
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM pago WHERE id_reserva = ?", Integer.class, idReserva));
    }
}
