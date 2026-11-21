package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionBusquedaRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;
import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicacionBusquedaFiltrosIntegracionTest {

    @Autowired
    private PublicacionBusquedaRepository busqueda;

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

    @BeforeEach
    void preparar() {
        when(pasarela.procesar(any(), any())).thenReturn(new ResultadoPagoSandbox(true, "pi_prueba", "Pago aprobado"));
        escenario = EscenarioReservaPrueba.crear(
                jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
    }

    @AfterEach
    void limpiar() {
        escenario.limpiar();
    }

    @Test
    void sinFiltrosEncuentraLaPublicacionActiva() {
        assertTrue(encuentra(criterio(null, null, null, null, null, null, List.of())));
    }

    @Test
    void filtraPorCiudadSinImportarTildesNiMayusculas() {
        assertTrue(encuentra(criterio("BOGOTÁ", null, null, null, null, null, List.of())));
        assertTrue(encuentra(criterio("bog", null, null, null, null, null, List.of())));
        assertFalse(encuentra(criterio("Medellín", null, null, null, null, null, List.of())));
    }

    @Test
    void filtraPorTipoCapacidadYPrecio() {
        assertTrue(encuentra(criterio(null, "apartamento", 2, new BigDecimal("50000"), new BigDecimal("150000"),
                null, List.of())));
        assertFalse(encuentra(criterio(null, "Casa", null, null, null, null, List.of())));
        assertFalse(encuentra(criterio(null, null, 3, null, null, null, List.of())));
        assertFalse(encuentra(criterio(null, null, null, new BigDecimal("150000"), null, null, List.of())));
        assertFalse(encuentra(criterio(null, null, null, null, new BigDecimal("50000"), null, List.of())));
    }

    @Test
    void exigeTodosLosServiciosPedidos() {
        UUID idServicio = UUID.randomUUID();
        jdbc.update("INSERT INTO servicio (id_servicio, nombre) VALUES (?, ?)", idServicio, "Prueba " + idServicio);
        try {
            assertFalse(encuentra(criterio(null, null, null, null, null, null, List.of(idServicio))));

            jdbc.update("INSERT INTO publicacion_servicio (id_publicacion, id_servicio) VALUES (?, ?)",
                    escenario.idPublicacion, idServicio);
            assertTrue(encuentra(criterio(null, null, null, null, null, null, List.of(idServicio))));
        } finally {
            jdbc.update("DELETE FROM publicacion_servicio WHERE id_servicio = ?", idServicio);
            jdbc.update("DELETE FROM servicio WHERE id_servicio = ?", idServicio);
        }
    }

    @Test
    void ocultaLaPublicacionSiLasFechasYaEstanReservadas() throws Exception {
        LocalDate entrada = LocalDate.now().plusDays(30);
        assertTrue(encuentra(criterio(null, null, null, null, null, entrada, List.of())));

        escenario.reservaConfirmada(30, 2);

        assertFalse(encuentra(criterio(null, null, null, null, null, entrada, List.of())));
        assertTrue(encuentra(criterio(null, null, null, null, null, entrada.plusDays(10), List.of())));
    }

    private boolean encuentra(PublicacionBusquedaCriteria criterio) {
        return busqueda.buscar(criterio).publicaciones().stream()
                .anyMatch(publicacion -> escenario.idPublicacion.equals(publicacion.getIdPublicacion()));
    }

    private static PublicacionBusquedaCriteria criterio(
            String ciudad,
            String tipo,
            Integer capacidad,
            BigDecimal precioMinimo,
            BigDecimal precioMaximo,
            LocalDate entrada,
            List<UUID> servicios) {
        return new PublicacionBusquedaCriteria(ciudad, entrada, entrada == null ? null : entrada.plusDays(2),
                capacidad, tipo, precioMinimo, precioMaximo, servicios, 0, 100);
    }
}
