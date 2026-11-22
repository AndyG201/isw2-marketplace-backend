package co.edu.unbosque.marketplace.compartido;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionBusquedaJdbcRepository;
import co.edu.unbosque.marketplace.reporte.infrastructure.persistence.DashboardIndicadoresJdbcRepository;
import co.edu.unbosque.marketplace.reserva.contracts.EstadoReserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.NocheReservaJdbcRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaCancelacionJdbcRepository;

/**
 * Las consultas SQL directas llevan los estados escritos como texto fijo (armar SQL dinámicamente es riesgoso y
 * SonarCloud lo marca). Esta prueba garantiza que esos textos sigan siendo exactamente los conjuntos de las
 * enumeraciones: si alguien cambia un conjunto del enum y olvida la consulta, falla.
 */
class ConsultasSqlUsanLosEstadosDelEnumTest {

    private static final Pattern LISTA_IN = Pattern.compile("IN \\(([^)]*)\\)");
    private static final Pattern IGUAL = Pattern.compile("estado = '([A-Z_]+)'");
    private static final Pattern ASIGNA = Pattern.compile("SET estado = '([A-Z_]+)'");

    private static String constante(Class<?> clase, String nombre) throws ReflectiveOperationException {
        Field campo = clase.getDeclaredField(nombre);
        campo.setAccessible(true);
        return (String) campo.get(null);
    }

    private static List<Set<String>> listasIn(String sql) {
        Matcher coincidencia = LISTA_IN.matcher(sql);
        List<Set<String>> listas = new java.util.ArrayList<>();
        while (coincidencia.find()) {
            Set<String> estados = new HashSet<>();
            for (String texto : coincidencia.group(1).split(",")) {
                estados.add(texto.trim().replace("'", ""));
            }
            listas.add(estados);
        }
        return listas;
    }

    private static Set<String> unicos(String sql, Pattern patron) {
        Matcher coincidencia = patron.matcher(sql);
        Set<String> estados = new HashSet<>();
        while (coincidencia.find()) {
            estados.add(coincidencia.group(1));
        }
        return estados;
    }

    @Test
    void lasNochesOcupadasUsanLosEstadosQueOcupanYLosQueRetienen() throws Exception {
        String sql = constante(NocheReservaJdbcRepository.class, "NOCHES_OCUPADAS");

        assertEquals(List.of(EstadoReserva.nombres(EstadoReserva.QUE_OCUPAN_NOCHES),
                EstadoReserva.nombres(EstadoReserva.CON_RETENCION)), listasIn(sql));
    }

    @Test
    void laLimpiezaDeNochesVencidasUsaLosEstadosQueLiberanYLosQueRetienen() throws Exception {
        String sql = constante(NocheReservaJdbcRepository.class, "LIMPIAR_NOCHES_VENCIDAS");

        assertEquals(List.of(EstadoReserva.nombres(EstadoReserva.QUE_LIBERAN_NOCHES),
                EstadoReserva.nombres(EstadoReserva.CON_RETENCION)), listasIn(sql));
    }

    @Test
    void laRetencionVigenteUsaLosEstadosQueOcupanYLosQueRetienen() throws Exception {
        String sql = constante(NocheReservaJdbcRepository.class, "RESERVA_CON_RETENCION_VIGENTE");

        assertEquals(List.of(EstadoReserva.nombres(EstadoReserva.QUE_OCUPAN_NOCHES),
                EstadoReserva.nombres(EstadoReserva.CON_RETENCION)), listasIn(sql));
    }

    @Test
    void cancelarMarcaCanceladaSoloLosEstadosCancelables() throws Exception {
        String sql = constante(ReservaCancelacionJdbcRepository.class, "MARCAR_CANCELADA");

        assertEquals(Set.of(EstadoReserva.CANCELADA.name()), unicos(sql, ASIGNA));
        assertEquals(List.of(EstadoReserva.nombres(EstadoReserva.CANCELABLES)), listasIn(sql));
    }

    @Test
    void laBusquedaYElTableroSoloContanPublicacionesActivas() throws Exception {
        Set<String> activa = Set.of(EstadoPublicacion.ACTIVA.name());

        assertEquals(activa, unicos(constante(PublicacionBusquedaJdbcRepository.class, "FILTROS"), IGUAL));
        assertEquals(activa,
                unicos(constante(DashboardIndicadoresJdbcRepository.class, "PUBLICACIONES_ACTIVAS"), IGUAL));
    }
}
