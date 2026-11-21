package co.edu.unbosque.marketplace.reserva;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.application.ReservaCompletadaService;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaConsultasIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @Autowired
    private ReservaCompletadaService completadas;

    @MockitoBean
    private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;
    private UUID idReserva;

    @BeforeEach
    void reservar() throws Exception {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        idReserva = escenario.reservaConfirmada(30, 2);
    }

    @AfterEach
    void limpiar() {
        escenario.limpiar();
    }

    @Test
    void elHuespedVeSuReservaConElMontoDeLaCotizacion() throws Exception {
        MvcResult lista = consultar("/reservas/mis-reservas", escenario.idHuesped);
        MvcResult detalle = consultar("/reservas/mis-reservas/" + idReserva, escenario.idHuesped);

        assertEquals(1, (Integer) campo(lista, "$.totalResultados"));
        assertEquals(idReserva.toString(), campo(lista, "$.reservas[0].idReserva"));
        assertEquals("Loft", campo(lista, "$.reservas[0].alojamiento"));
        assertMonto("220000", campo(lista, "$.reservas[0].montoTotal"));
        assertEquals(Boolean.FALSE, campo(lista, "$.reservas[0].resenada"));
        assertEquals(escenario.idAlojamiento.toString(), campo(detalle, "$.idAlojamiento"));
        assertMonto("220000", campo(detalle, "$.montoTotal"));
    }

    @Test
    void elAnfitrionVeLaReservaConElNombreDelHuesped() throws Exception {
        MvcResult lista = consultar("/reservas/mis-alojamientos", escenario.idAnfitrion);
        MvcResult detalle = consultar("/reservas/mis-alojamientos/" + idReserva, escenario.idAnfitrion);

        assertEquals(1, (Integer) campo(lista, "$.totalResultados"));
        assertEquals("Prueba Reserva", campo(lista, "$.reservas[0].huesped"));
        assertEquals(escenario.idHuesped.toString(), campo(lista, "$.reservas[0].idHuesped"));
        assertMonto("220000", campo(lista, "$.reservas[0].montoTotal"));
        assertEquals("CONFIRMADA", campo(detalle, "$.estado"));
    }

    @Test
    void unaReservaConfirmadaSeCompletaCuandoPasaLaFechaDeSalida() {
        jdbc.update("""
                UPDATE cotizacion SET fecha_entrada = current_date - 3, fecha_salida = current_date - 1
                WHERE id_cotizacion = (SELECT id_cotizacion FROM reserva WHERE id_reserva = ?)
                """, idReserva);

        completadas.completarReservasFinalizadas(LocalDate.now());

        assertEquals("COMPLETADA", escenario.estadoReserva(idReserva));
    }

    private MvcResult consultar(String ruta, UUID idUsuario) throws Exception {
        MvcResult resultado = mockMvc.perform(get(ruta).header("Authorization", escenario.token(idUsuario)))
                .andReturn();
        assertEquals(200, resultado.getResponse().getStatus());
        return resultado;
    }

    private static void assertMonto(String esperado, Object valor) {
        assertEquals(0, new BigDecimal(esperado).compareTo(new BigDecimal(valor.toString())));
    }

    private static <T> T campo(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), ruta);
    }
}
