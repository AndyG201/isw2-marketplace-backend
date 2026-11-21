package co.edu.unbosque.marketplace.auditoria.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;

@ExtendWith(MockitoExtension.class)
class AuditoriaConsultaJdbcRepositoryTest {

    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private AuditoriaConsultaJdbcRepository repository;

    private EventoAuditoriaIndice indice;

    @BeforeEach
    void setUp() {

        indice = new EventoAuditoriaIndice(
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-11-20T10:15:30Z"),
                "PUBLICACION_APROBADA",
                "EXITOSO",
                "PUBLICACION",
                UUID.randomUUID(),
                "corr-test",
                "eventos/2026/11/20/evento.json",
                "a".repeat(64),
                "version-123"
        );
    }

    @Test
    void consultarAplicaFiltrosYOrdenamiento() {

        FiltrosAuditoria filtros = new FiltrosAuditoria(
                indice.idUsuario(),
                indice.fechaEvento().minusDays(1),
                indice.fechaEvento().plusDays(1),
                "PUBLICACION_APROBADA",
                "EXITOSO",
                "PUBLICACION"
        );

        doReturn(List.of(indice))
                .when(jdbc)
                .query(anyString(), any(RowMapper.class), any(Object[].class));

        List<EventoAuditoriaIndice> resultado =
                repository.consultar(filtros, 25, 50L);

        assertEquals(1, resultado.size());
        assertEquals(indice.idEvento(), resultado.get(0).idEvento());
        assertEquals(indice.claveObjeto(), resultado.get(0).claveObjeto());
        assertEquals(indice.sha256(), resultado.get(0).sha256());

        ArgumentCaptor<String> sql =
                ArgumentCaptor.forClass(String.class);

        verify(jdbc).query(
                sql.capture(),
                any(RowMapper.class),
                any(Object[].class)
        );

        assertTrue(sql.getValue().contains("public.evento_auditoria"));
        assertTrue(sql.getValue().contains("id_usuario = ?"));
        assertTrue(sql.getValue().contains("fecha_evento >= ?"));
        assertTrue(sql.getValue().contains("fecha_evento <= ?"));
        assertTrue(sql.getValue().contains("ORDER BY fecha_evento DESC"));
        assertTrue(sql.getValue().contains("LIMIT ? OFFSET ?"));
    }

    @Test
    void contarDevuelveElTotal() {

        when(jdbc.queryForObject(
                anyString(),
                eq(Long.class),
                any(Object[].class)
        )).thenReturn(7L);

        long total = repository.contar(
                new FiltrosAuditoria(null, null, null, null, null, null)
        );

        assertEquals(7L, total);
    }

    @Test
    void buscarDevuelveElIndiceEncontrado() {

        doReturn(List.of(indice))
                .when(jdbc)
                .query(anyString(), any(RowMapper.class), any(Object[].class));

        Optional<EventoAuditoriaIndice> resultado =
                repository.buscar(indice.idEvento());

        assertTrue(resultado.isPresent());
        assertEquals(indice.idEvento(), resultado.get().idEvento());
        assertEquals(indice.versionObjeto(), resultado.get().versionObjeto());
    }

    @Test
    void buscarDevuelveVacioCuandoNoExiste() {

        doReturn(List.of())
                .when(jdbc)
                .query(anyString(), any(RowMapper.class), any(Object[].class));

        assertTrue(repository.buscar(UUID.randomUUID()).isEmpty());
    }

    @Test
    void rechazaParametrosDePaginacionInvalidos() {

        FiltrosAuditoria filtros =
                new FiltrosAuditoria(null, null, null, null, null, null);

        assertThrows(
                DatosInvalidosException.class,
                () -> repository.consultar(filtros, 0, 0)
        );

        assertThrows(
                DatosInvalidosException.class,
                () -> repository.consultar(filtros, 10, -1)
        );
    }
}
