package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.auditoria.application.port.AuditoriaConsultaRepository;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;

/**
 * La consulta de auditoría usa siempre el mismo SQL, con cada filtro opcional. Con una base de datos real se
 * comprueba que cada filtro, solo o combinado, devuelve exactamente lo esperado.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuditoriaConsultaFiltrosBaseDatosTest {

    private static final OffsetDateTime BASE = OffsetDateTime.parse("2031-05-10T12:00:00Z");

    @Autowired private AuditoriaConsultaRepository consulta;
    @Autowired private JdbcTemplate jdbc;

    private final String accion = "PRUEBA_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    private final UUID usuarioA = UUID.randomUUID();
    private final UUID usuarioB = UUID.randomUUID();
    private final UUID eventoUno = UUID.randomUUID();
    private final UUID eventoDos = UUID.randomUUID();
    private final UUID eventoTres = UUID.randomUUID();

    @BeforeEach
    void insertarEventos() {
        insertar(eventoUno, usuarioA, BASE, "EXITOSO", "RESERVA");
        insertar(eventoDos, usuarioB, BASE.plusDays(1), "FALLIDO", "RESERVA");
        insertar(eventoTres, usuarioA, BASE.plusDays(2), "EXITOSO", "PUBLICACION");
    }

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM public.evento_auditoria WHERE accion = ?", accion);
    }

    private void insertar(UUID id, UUID usuario, OffsetDateTime fecha, String resultado, String entidad) {
        jdbc.update("""
                INSERT INTO public.evento_auditoria (id_evento, id_usuario, fecha_evento, accion, resultado,
                    entidad_afectada, clave_objeto, sha256, modo_retencion, retencion_hasta)
                VALUES (?, ?, ?, ?, ?, ?, 'eventos/prueba.json', ?, 'COMPLIANCE', ?)
                """, id, usuario, fecha, accion, resultado, entidad, "a".repeat(64), BASE.plusYears(1));
    }

    private List<UUID> consultar(FiltrosAuditoria filtros) {
        return consulta.consultar(filtros, 50, 0).stream().map(EventoAuditoriaIndice::idEvento).toList();
    }

    private FiltrosAuditoria filtros(UUID usuario, OffsetDateTime desde, OffsetDateTime hasta, String resultado,
            String entidad) {
        return new FiltrosAuditoria(usuario, desde, hasta, accion, resultado, entidad);
    }

    @Test
    void sinOtrosFiltrosDevuelveLosEventosDeLaAccionDelMasNuevoAlMasViejo() {
        assertEquals(List.of(eventoTres, eventoDos, eventoUno), consultar(filtros(null, null, null, null, null)));
        assertEquals(3, consulta.contar(filtros(null, null, null, null, null)));
    }

    @Test
    void filtraPorUsuario() {
        assertEquals(List.of(eventoTres, eventoUno), consultar(filtros(usuarioA, null, null, null, null)));
        assertEquals(List.of(eventoDos), consultar(filtros(usuarioB, null, null, null, null)));
        assertEquals(1, consulta.contar(filtros(usuarioB, null, null, null, null)));
    }

    @Test
    void filtraPorRangoDeFechas() {
        assertEquals(List.of(eventoTres, eventoDos),
                consultar(filtros(null, BASE.plusHours(1), null, null, null)));
        assertEquals(List.of(eventoDos, eventoUno),
                consultar(filtros(null, null, BASE.plusDays(1), null, null)));
        assertEquals(List.of(eventoDos), consultar(filtros(null, BASE.plusHours(1), BASE.plusDays(1), null, null)));
    }

    @Test
    void filtraPorResultadoYEntidadYLosCombina() {
        assertEquals(List.of(eventoTres, eventoUno), consultar(filtros(null, null, null, "EXITOSO", null)));
        assertEquals(List.of(eventoTres), consultar(filtros(null, null, null, null, "PUBLICACION")));
        assertEquals(List.of(eventoUno), consultar(filtros(usuarioA, null, null, "EXITOSO", "RESERVA")));
        assertEquals(List.of(), consultar(filtros(usuarioB, null, null, "EXITOSO", null)));
    }

    @Test
    void ignoraLosEspaciosYLosTextosEnBlanco() {
        FiltrosAuditoria conEspacios = new FiltrosAuditoria(null, null, null, "  " + accion + "  ", "   ", "");
        assertEquals(3, consultar(conEspacios).size());
    }

    @Test
    void paginaSinPerderNiRepetirEventos() {
        FiltrosAuditoria solo = filtros(null, null, null, null, null);
        assertEquals(List.of(eventoTres, eventoDos), consulta.consultar(solo, 2, 0).stream()
                .map(EventoAuditoriaIndice::idEvento).toList());
        assertEquals(List.of(eventoUno), consulta.consultar(solo, 2, 2).stream()
                .map(EventoAuditoriaIndice::idEvento).toList());
    }
}
