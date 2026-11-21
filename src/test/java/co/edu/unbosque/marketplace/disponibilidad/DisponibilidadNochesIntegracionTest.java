package co.edu.unbosque.marketplace.disponibilidad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DisponibilidadNochesIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @Autowired
    private CotizacionDisponibilidadQuery disponibilidad;

    @MockitoBean
    private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;
    private final LocalDate entrada = LocalDate.now().plusDays(30);

    @BeforeEach
    void reservarDosNoches() throws Exception {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        escenario.reservaConfirmada(30, 2);
    }

    @AfterEach
    void limpiar() {
        escenario.limpiar();
    }

    @Test
    void laConsultaPublicaMuestraLasNochesReservadas() throws Exception {
        MvcResult respuesta = mockMvc.perform(get("/alojamientos/{id}/disponibilidad", escenario.idAlojamiento)
                        .param("fechaInicio", entrada.minusDays(1).toString())
                        .param("fechaFin", entrada.plusDays(3).toString()))
                .andReturn();

        assertEquals(200, respuesta.getResponse().getStatus());
        assertEquals(List.of(entrada.toString(), entrada.plusDays(1).toString()),
                campo(respuesta, "$.nochesOcupadas"));
        assertEquals(Boolean.FALSE, campo(respuesta, "$.disponible"));
    }

    @Test
    void elCalendarioDelAnfitrionMuestraTodasLasNochesReservadas() throws Exception {
        MvcResult respuesta = mockMvc.perform(get("/alojamientos/{id}/calendario", escenario.idAlojamiento)
                        .header("Authorization", escenario.token(escenario.idAnfitrion)))
                .andReturn();

        assertEquals(200, respuesta.getResponse().getStatus());
        assertEquals(List.of(entrada.toString(), entrada.plusDays(1).toString()),
                campo(respuesta, "$.nochesOcupadas"));
    }

    @Test
    void laCotizacionSoloEstaDisponibleEnNochesLibresDentroDeLosPeriodos() {
        assertFalse(disponibilidad.estaDisponible(escenario.idAlojamiento, entrada, entrada.plusDays(2)));
        assertTrue(disponibilidad.estaDisponible(escenario.idAlojamiento, entrada.plusDays(10), entrada.plusDays(12)));
        assertFalse(disponibilidad.estaDisponible(escenario.idAlojamiento, entrada.plusDays(200), entrada.plusDays(202)));
        assertFalse(disponibilidad.estaDisponible(escenario.idAlojamiento, entrada, entrada));
    }

    @Test
    void noSePuedeDeshabilitarUnPeriodoConNochesReservadas() throws Exception {
        UUID idPeriodo = jdbc.queryForObject(
                "SELECT id_periodo FROM periodo_disponibilidad WHERE id_alojamiento = ?",
                UUID.class, escenario.idAlojamiento);

        MvcResult respuesta = mockMvc.perform(delete("/alojamientos/{id}/periodos-disponibles/{periodo}",
                        escenario.idAlojamiento, idPeriodo)
                        .header("Authorization", escenario.token(escenario.idAnfitrion)))
                .andReturn();

        assertEquals(409, respuesta.getResponse().getStatus());
        assertEquals(Boolean.TRUE, jdbc.queryForObject(
                "SELECT habilitado FROM periodo_disponibilidad WHERE id_periodo = ?", Boolean.class, idPeriodo));
    }

    private static <T> T campo(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), ruta);
    }
}
