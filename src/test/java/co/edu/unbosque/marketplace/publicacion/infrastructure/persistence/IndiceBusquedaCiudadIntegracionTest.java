package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import co.edu.unbosque.marketplace.reserva.EscenarioReservaPrueba;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

/**
 * La búsqueda por ciudad usa una columna normalizada (sin tildes ni mayúsculas) con índice de trigramas,
 * para que el filtro LIKE '%texto%' no recorra toda la tabla.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndiceBusquedaCiudadIntegracionTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService usuarios;
    @Autowired private PlatformTransactionManager transacciones;

    private EscenarioReservaPrueba escenario;

    @BeforeEach
    void preparar() {
        escenario = EscenarioReservaPrueba.crear(jdbc, mockMvc, jwtUtil, usuarios, "INMEDIATA", "FLEXIBLE", 100, 24);
    }

    @AfterEach
    void limpiar() {
        escenario.limpiar();
    }

    @Test
    void laColumnaGeneradaGuardaLaCiudadSinTildesNiMayusculas() {
        UUID idAlojamiento = escenario.idAlojamiento;
        jdbc.update("UPDATE alojamiento SET ciudad = ? WHERE id_alojamiento = ?", "MedellÍn Ñandú", idAlojamiento);

        // La columna se recalcula sola cuando cambia la ciudad.
        assertEquals("medellin nandu", jdbc.queryForObject(
                "SELECT ciudad_normalizada FROM alojamiento WHERE id_alojamiento = ?", String.class, idAlojamiento));
        assertEquals("medellin nandu", jdbc.queryForObject(
                "SELECT ciudad_normalizada FROM vista_alojamiento WHERE id_alojamiento = ?", String.class, idAlojamiento));
    }

    @Test
    void existeUnIndiceDeTrigramasSobreLaColumnaNormalizada() {
        String definicion = jdbc.queryForObject(
                "SELECT indexdef FROM pg_indexes WHERE indexname = 'idx_alojamiento_ciudad_trgm'", String.class);

        assertTrue(definicion.contains("gin"), definicion);
        assertTrue(definicion.contains("gin_trgm_ops"), definicion);
        assertTrue(definicion.contains("ciudad_normalizada"), definicion);
    }

    @Test
    void elPlanificadorPuedeUsarElIndiceParaElFiltroDeLaBusqueda() {
        List<String> plan = new TransactionTemplate(transacciones).execute(estado -> {
            // Con la tabla vacía o pequeña el planificador prefiere recorrerla; se le impide para comprobar que el
            // filtro de la consulta es compatible con el índice.
            jdbc.execute("SET LOCAL enable_seqscan = off");
            return jdbc.queryForList(
                    "EXPLAIN SELECT 1 FROM public.vista_alojamiento a "
                            + "WHERE a.ciudad_normalizada LIKE '%bog%' ESCAPE '\\'", String.class);
        });

        assertTrue(String.join("\n", plan).contains("idx_alojamiento_ciudad_trgm"),
                "El plan no usa el índice de trigramas:\n" + String.join("\n", plan));
    }
}
