package co.edu.unbosque.marketplace.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import java.util.Map;
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

import co.edu.unbosque.marketplace.notificacion.application.NotificacionCancelacionReconciliationWorker;
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificacionCancelacionIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService usuarios;

    @Autowired
    private NotificacionCancelacionReconciliationWorker reconciliacion;

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
    void avisaAlHuespedYAlAnfitrionDeUnaCancelacionSinRepetirLaNotificacion() throws Exception {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        UUID idReserva = escenario.reservaConfirmada(30, 2);
        mockMvc.perform(post("/reservas/{id}/cancelacion", idReserva)
                        .header("Authorization", escenario.token(escenario.idHuesped))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Cambio de planes\"}"))
                .andReturn();

        reconciliacion.registrarCancelacionesSinNotificacion();
        reconciliacion.registrarCancelacionesSinNotificacion();

        List<Map<String, Object>> avisos = jdbc.queryForList("""
                SELECT id_usuario, id_publicacion, mensaje FROM notificacion
                WHERE id_reserva = ? AND tipo = 'RESERVA_CANCELADA'
                ORDER BY mensaje
                """, idReserva);
        assertEquals(2, avisos.size());
        assertEquals(escenario.idAnfitrion, avisos.get(1).get("id_usuario"));
        assertEquals("Una reserva de tu alojamiento fue cancelada.", avisos.get(1).get("mensaje"));
        assertEquals(escenario.idHuesped, avisos.get(0).get("id_usuario"));
        assertEquals("Tu reserva fue cancelada. Motivo: Cambio de planes", avisos.get(0).get("mensaje"));
        assertEquals(escenario.idPublicacion, avisos.get(0).get("id_publicacion"));
    }
}
