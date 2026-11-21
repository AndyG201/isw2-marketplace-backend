package co.edu.unbosque.marketplace.auditoria.application;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.auditoria.api.response.EventoAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.api.response.PaginaAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.AuditoriaConsultaJdbcRepository;
import co.edu.unbosque.marketplace.auditoria.infrastructure.storage.BackblazeAuditoriaService;

@ExtendWith(MockitoExtension.class)
class AuditoriaConsultaServiceTest {

    @Mock
    private AuditoriaConsultaJdbcRepository repository;

    @Mock
    private ObjectProvider<BackblazeAuditoriaService> backblazeProvider;

    @Mock
    private BackblazeAuditoriaService backblaze;

    @InjectMocks
    private AuditoriaConsultaService service;

    private UUID idEvento;
    private UUID idUsuario;
    private UUID idEntidad;
    private OffsetDateTime fechaEvento;
    private EventoAuditoria evento;
    private EventoAuditoriaIndice indice;

    @BeforeEach
    void setUp() {

        lenient().when(backblazeProvider.getIfAvailable())
                .thenReturn(backblaze);

        idEvento = UUID.randomUUID();
        idUsuario = UUID.randomUUID();
        idEntidad = UUID.randomUUID();

        fechaEvento = OffsetDateTime.parse(
                "2026-11-20T10:15:30-05:00");

        evento = new EventoAuditoria(
                idEvento,
                idUsuario,
                fechaEvento,
                "PUBLICACION_APROBADA",
                "EXITOSO",
                "PUBLICACION",
                idEntidad,
                "{\"estado\":\"ACTIVA\"}",
                "correlacion-123"
        );

        indice = new EventoAuditoriaIndice(
                idEvento,
                idUsuario,
                fechaEvento,
                "PUBLICACION_APROBADA",
                "EXITOSO",
                "PUBLICACION",
                idEntidad,
                "correlacion-123",
                "eventos/2026/11/20/" + idEvento + ".json",
                "a".repeat(64),
                "version-123"
        );
    }

    @Test
    void consultaFiltradaDevuelvePaginaYNormalizaLosFiltros() {

        OffsetDateTime desde = fechaEvento.minusDays(1);
        OffsetDateTime hasta = fechaEvento.plusDays(1);

        when(repository.contar(any())).thenReturn(21L);

        when(repository.consultar(
                any(),
                eq(10),
                eq(20L)
        )).thenReturn(List.of(indice));

        when(backblaze.obtenerEvento(
                indice.claveObjeto(),
                indice.versionObjeto(),
                indice.sha256(),
                indice.idEvento()
        )).thenReturn(evento);

        PaginaAuditoriaResponse respuesta = service.consultar(
                idUsuario,
                desde,
                hasta,
                " PUBLICACION_APROBADA ",
                " EXITOSO ",
                " PUBLICACION ",
                2,
                10
        );

        assertEquals(1, respuesta.eventos().size());

        EventoAuditoriaResponse respuestaEvento =
                respuesta.eventos().get(0);

        assertEquals(idEvento, respuestaEvento.idEvento());
        assertEquals(idUsuario, respuestaEvento.idUsuario());
        assertEquals("PUBLICACION_APROBADA", respuestaEvento.accion());
        assertEquals("{\"estado\":\"ACTIVA\"}", respuestaEvento.detalleJson());

        assertEquals(2, respuesta.pagina());
        assertEquals(10, respuesta.tamano());
        assertEquals(21L, respuesta.totalResultados());
        assertEquals(3, respuesta.totalPaginas());

        verify(backblaze).obtenerEvento(
                indice.claveObjeto(),
                indice.versionObjeto(),
                indice.sha256(),
                indice.idEvento()
        );
    }

    @Test
    void consultaSinResultadosDevuelveListaVaciaYMensaje() {

        when(repository.contar(any())).thenReturn(0L);
        when(repository.consultar(any(), eq(20), eq(0L)))
                .thenReturn(List.of());

        PaginaAuditoriaResponse respuesta = service.consultar(
                null, null, null, null, null, null, 0, 20);

        assertTrue(respuesta.eventos().isEmpty());
        assertEquals(0L, respuesta.totalResultados());
        assertEquals(0, respuesta.totalPaginas());

        assertEquals(
                "No hay eventos de auditoría para los filtros indicados.",
                respuesta.mensaje()
        );

        verifyNoInteractions(backblaze);
    }

    @Test
    void consultaConDesdePosteriorAHastaEsRechazada() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        null,
                        fechaEvento,
                        fechaEvento.minusDays(1),
                        null,
                        null,
                        null,
                        0,
                        20
                )
        );

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository, backblaze);
    }

    @Test
    void consultaConPaginaNegativaEsRechazada() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        null, null, null, null, null, null, -1, 20)
        );

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void consultaConTamanoFueraDelLimiteEsRechazada() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        null, null, null, null, null, null, 0, 101)
        );

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void consultaDevuelve503SiPostgresNoEstaDisponible() {

        when(repository.contar(any()))
                .thenThrow(new DataAccessResourceFailureException(
                        "PostgreSQL no disponible"));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        null, null, null, null, null, null, 0, 20)
        );

        assertEquals(503, error.getStatusCode().value());
    }

    @Test
    void consultaDevuelve503SiBackblazeNoEstaDisponible() {

        when(repository.contar(any())).thenReturn(1L);
        when(repository.consultar(any(), eq(20), eq(0L)))
                .thenReturn(List.of(indice));

        when(backblaze.obtenerEvento(
                anyString(),
                anyString(),
                anyString(),
                any(UUID.class)
        )).thenThrow(new IllegalStateException("Backblaze no disponible"));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        null, null, null, null, null, null, 0, 20)
        );

        assertEquals(503, error.getStatusCode().value());
    }

    @Test
    void consultaPorIdDevuelveElEventoEncontrado() {

        when(repository.buscar(idEvento))
                .thenReturn(Optional.of(indice));

        when(backblaze.obtenerEvento(
                indice.claveObjeto(),
                indice.versionObjeto(),
                indice.sha256(),
                indice.idEvento()
        )).thenReturn(evento);

        EventoAuditoriaResponse respuesta =
                service.consultar(idEvento);

        assertEquals(idEvento, respuesta.idEvento());
        assertEquals(idUsuario, respuesta.idUsuario());
        assertEquals(fechaEvento, respuesta.fechaEvento());
        assertEquals("PUBLICACION_APROBADA", respuesta.accion());
        assertEquals("EXITOSO", respuesta.resultado());
        assertEquals("{\"estado\":\"ACTIVA\"}", respuesta.detalleJson());
    }

    @Test
    void consultaPorIdInexistenteDevuelve404() {

        when(repository.buscar(idEvento))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(idEvento)
        );

        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(backblaze);
    }

    @Test
    void consultaPorIdNuloDevuelve400() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar((UUID) null)
        );

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void consultaPorIdDevuelve503SiBackblazeFalla() {

        when(repository.buscar(idEvento))
                .thenReturn(Optional.of(indice));

        when(backblaze.obtenerEvento(
                anyString(),
                anyString(),
                anyString(),
                any(UUID.class)
        )).thenThrow(new IllegalStateException("Error de lectura"));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(idEvento)
        );

        assertEquals(503, error.getStatusCode().value());
    }

    @Test
    void consultaDevuelve503SiBackblazeNoEstaConfigurado() {

        when(repository.contar(any())).thenReturn(1L);
        when(repository.consultar(any(), eq(20), eq(0L)))
                .thenReturn(List.of(indice));

        when(backblazeProvider.getIfAvailable()).thenReturn(null);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        null, null, null, null, null, null, 0, 20)
        );

        assertEquals(503, error.getStatusCode().value());
    }
}
