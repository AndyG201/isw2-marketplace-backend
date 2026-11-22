package co.edu.unbosque.marketplace.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepositoryJpa;
import co.edu.unbosque.marketplace.reserva.contracts.EstadoReserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaConfirmacionRepositoryJpa;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepositoryJpa;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRetencionRepositoryJpa;

/**
 * Los estados viven en dos sitios: las enumeraciones (código) y las restricciones CHECK de la base de datos. Si alguien
 * agrega un estado en uno y olvida el otro, estas pruebas fallan. También revisan los textos de estado que quedan
 * escritos dentro de consultas {@code @Query}, que por ser constantes de anotación no pueden usar la enumeración.
 */
@SpringBootTest
@ActiveProfiles("test")
class EstadosCoincidenConLaBaseDeDatosIntegracionTest {

    private static final Pattern TEXTO_ENTRE_COMILLAS = Pattern.compile("'([A-Z_]+)'");

    @Autowired private JdbcTemplate jdbc;

    @Test
    void losEstadosDeReservaSonLosQueAceptaLaBaseDeDatos() {
        assertEquals(new HashSet<>(EstadoReserva.nombres()), estadosPermitidos("chk_reserva_estado"));
    }

    @Test
    void losEstadosDePublicacionSonLosQueAceptaLaBaseDeDatos() {
        Set<String> enCodigo = new HashSet<>();
        Arrays.stream(EstadoPublicacion.values()).forEach(estado -> enCodigo.add(estado.name()));
        assertEquals(enCodigo, estadosPermitidos("chk_publicacion_estado"));
    }

    @Test
    void losTextosDeEstadoDeLasConsultasJpaExisten() {
        Set<String> deReserva = textosDeConsultas(ReservaRepositoryJpa.class, ReservaConfirmacionRepositoryJpa.class,
                ReservaRetencionRepositoryJpa.class);
        assertTrue(EstadoReserva.nombres().containsAll(deReserva), "estados de reserva desconocidos: " + deReserva);
        Set<String> dePublicacion = textosDeConsultas(PublicacionRepositoryJpa.class);
        assertTrue(EstadoPublicacion.nombres(java.util.EnumSet.allOf(EstadoPublicacion.class)).containsAll(dePublicacion),
                "estados de publicación desconocidos: " + dePublicacion);
    }

    @Test
    void lasConsultasJpaSiguenUsandoEstadosDeSuTipo() {
        // Se espera encontrar textos de estado: si esta lista queda vacía, la prueba anterior no estaría comprobando nada.
        assertFalse(textosDeConsultas(ReservaRepositoryJpa.class, ReservaConfirmacionRepositoryJpa.class,
                ReservaRetencionRepositoryJpa.class).isEmpty());
        assertFalse(textosDeConsultas(PublicacionRepositoryJpa.class).isEmpty());
    }

    private Set<String> estadosPermitidos(String restriccion) {
        String definicion = jdbc.queryForObject(
                "SELECT pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = ?", String.class, restriccion);
        Set<String> estados = new HashSet<>();
        Matcher coincidencia = Pattern.compile("'([A-Z_]+)'::").matcher(definicion);
        while (coincidencia.find()) {
            estados.add(coincidencia.group(1));
        }
        return estados;
    }

    private static Set<String> textosDeConsultas(Class<?>... repositorios) {
        Set<String> textos = new HashSet<>();
        for (Class<?> repositorio : repositorios) {
            for (Method metodo : repositorio.getDeclaredMethods()) {
                Query consulta = metodo.getAnnotation(Query.class);
                if (consulta == null) {
                    continue;
                }
                Matcher coincidencia = TEXTO_ENTRE_COMILLAS.matcher(consulta.value());
                while (coincidencia.find()) {
                    textos.add(coincidencia.group(1));
                }
            }
        }
        return textos;
    }
}
