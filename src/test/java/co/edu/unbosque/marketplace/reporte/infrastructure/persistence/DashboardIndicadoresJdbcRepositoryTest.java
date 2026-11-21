package co.edu.unbosque.marketplace.reporte.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
import co.edu.unbosque.marketplace.reporte.application.port.DashboardIndicadoresRepository;
import co.edu.unbosque.marketplace.reporte.application.port.DashboardIndicadoresRepository.CiudadDemanda;
import co.edu.unbosque.marketplace.reporte.application.port.DashboardIndicadoresRepository.IndicadoresUsuarios;
import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardIndicadoresJdbcRepositoryTest {

    @Autowired
    private DashboardIndicadoresRepository indicadores;

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
    void calculaLosIndicadoresConLosDatosRegistrados() throws Exception {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
        escenario.reservaConfirmada(30, 2);

        IndicadoresUsuarios cuentas = indicadores.consultarIndicadoresUsuarios();
        Map<String, Long> reservas = indicadores.contarReservasPorEstado();
        List<CiudadDemanda> ciudades = indicadores.consultarCiudadesConMayorDemanda();

        assertTrue(cuentas.total() >= 3);
        assertTrue(cuentas.huespedes() >= 2);
        assertTrue(cuentas.anfitriones() >= 1);
        assertTrue(indicadores.contarPublicacionesActivas() >= 1);
        assertTrue(reservas.get("CONFIRMADA") >= 1);
        assertTrue(indicadores.calcularIngresosEstimados().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(ciudades.stream().anyMatch(ciudad -> "Bogotá".equals(ciudad.ciudad())
                && ciudad.solicitudesCotizacion() >= 1));
        assertTrue(cuentas.total() >= cuentas.activos());
    }
}
