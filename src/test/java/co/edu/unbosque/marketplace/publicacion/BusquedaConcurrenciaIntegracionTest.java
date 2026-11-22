package co.edu.unbosque.marketplace.publicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

/**
 * Carga ligera dentro de la suite: muchas búsquedas simultáneas contra la aplicación completa (seguridad, PostgreSQL
 * y Redis reales). No mide tiempos, comprueba que ninguna falla y que la misma consulta da siempre el mismo resultado,
 * con la caché de por medio. La prueba de carga con tiempos está en pruebas-carga/busqueda.js (k6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BusquedaConcurrenciaIntegracionTest {

    private static final int HILOS = 16;
    private static final int PETICIONES_POR_HILO = 20;

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService usuarios;

    private EscenarioReservaPrueba escenario;

    @BeforeEach
    void preparar() {
        escenario = EscenarioReservaPrueba.crear(jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
    }

    @AfterEach
    void limpiar() {
        escenario.limpiar();
    }

    private static final String[] CONSULTAS = {
            "/publicaciones/busqueda?ciudad=bogota",
            "/publicaciones/busqueda?ciudad=BOGOTÁ",
            "/publicaciones/busqueda?ciudad=bog&tipoAlojamiento=apartamento",
            "/publicaciones/busqueda?capacidad=2&pagina=0&tamano=10",
            "/publicaciones/busqueda?ciudad=ciudad-que-no-existe",
            "/publicaciones/catalogo",
    };

    @Test
    void variasBusquedasSimultaneasNoFallanYDanElMismoResultadoParaLaMismaConsulta() throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(HILOS);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<List<String>>> trabajos = new ArrayList<>();
        try {
            for (int h = 0; h < HILOS; h++) {
                final int desfase = h;
                Callable<List<String>> trabajo = () -> {
                    salida.await();
                    List<String> resultados = new ArrayList<>();
                    for (int i = 0; i < PETICIONES_POR_HILO; i++) {
                        String consulta = CONSULTAS[(i + desfase) % CONSULTAS.length];
                        MvcResult r = mockMvc.perform(get(consulta)).andReturn();
                        assertEquals(200, r.getResponse().getStatus(), "falló " + consulta);
                        resultados.add(consulta + "=>" + r.getResponse().getContentAsString(StandardCharsets.UTF_8));
                    }
                    return resultados;
                };
                trabajos.add(hilos.submit(trabajo));
            }
            salida.countDown();

            // Para cada consulta, todas las respuestas deben ser idénticas.
            java.util.Map<String, Set<String>> respuestasPorConsulta = new ConcurrentHashMap<>();
            int total = 0;
            for (Future<List<String>> trabajo : trabajos) {
                for (String linea : trabajo.get(120, TimeUnit.SECONDS)) {
                    int corte = linea.indexOf("=>");
                    respuestasPorConsulta.computeIfAbsent(linea.substring(0, corte), k -> ConcurrentHashMap.newKeySet())
                            .add(linea.substring(corte + 2));
                    total++;
                }
            }

            assertEquals(HILOS * PETICIONES_POR_HILO, total);
            assertEquals(CONSULTAS.length, respuestasPorConsulta.size());
            respuestasPorConsulta.forEach((consulta, distintas) ->
                    assertEquals(1, distintas.size(), "la consulta " + consulta + " dio respuestas distintas"));
            // La consulta sobre la ciudad del escenario encuentra su publicación; la inexistente no encuentra nada.
            assertTrue(respuestasPorConsulta.get("/publicaciones/busqueda?ciudad=bogota").iterator().next()
                    .contains(escenario.idPublicacion.toString()));
            assertTrue(respuestasPorConsulta.get("/publicaciones/busqueda?ciudad=ciudad-que-no-existe").iterator().next()
                    .contains("\"publicaciones\":[]"));
        } finally {
            hilos.shutdownNow();
        }
    }
}
