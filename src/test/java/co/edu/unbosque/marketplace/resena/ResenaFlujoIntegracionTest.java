package co.edu.unbosque.marketplace.resena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResenaFlujoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @MockitoBean
    private PagoSandboxGateway pasarela;

    private EscenarioReservaPrueba escenario;

    @AfterEach
    void limpiar() {
        if (escenario != null) {
            escenario.limpiar();
        }
    }

    @Test
    void elHuespedResenaUnaReservaCompletadaYLaResenaQuedaPublica() throws Exception {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        UUID idReserva = escenario.reservaConfirmada(30, 2);
        jdbc.update("UPDATE reserva SET estado = 'COMPLETADA' WHERE id_reserva = ?", idReserva);

        MvcResult creada = mockMvc.perform(post("/reservas/{id}/resenas", idReserva)
                        .header("Authorization", escenario.token(escenario.idHuesped))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":4,\"comentario\":\"Muy buena ubicación\"}"))
                .andReturn();

        assertEquals(201, creada.getResponse().getStatus());
        assertEquals(escenario.idPublicacion.toString(), campo(creada, "$.idPublicacion"));
        assertEquals(escenario.idAlojamiento.toString(), campo(creada, "$.idAlojamiento"));

        MvcResult publicas = mockMvc.perform(get("/publicaciones/{id}/resenas", escenario.idPublicacion))
                .andReturn();

        assertEquals(200, publicas.getResponse().getStatus());
        assertEquals(Integer.valueOf(1), campo(publicas, "$.totalResenas"));
        assertEquals("Muy buena ubicación", campo(publicas, "$.resenas[0].comentario"));
        assertEquals(Integer.valueOf(4), campo(publicas, "$.resenas[0].calificacion"));
    }

    @Test
    void lasResenasDeUnaPublicacionInexistenteRespondenNoEncontrado() throws Exception {
        MvcResult respuesta = mockMvc.perform(get("/publicaciones/{id}/resenas", UUID.randomUUID())).andReturn();

        assertEquals(404, respuesta.getResponse().getStatus());
        assertEquals("La publicación no existe.", campo(respuesta, "$.mensaje"));
    }

    private static <T> T campo(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), ruta);
    }
}
