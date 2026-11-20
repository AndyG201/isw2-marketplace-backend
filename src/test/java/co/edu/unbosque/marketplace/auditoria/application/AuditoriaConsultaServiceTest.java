package co.edu.unbosque.marketplace.auditoria.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.auditoria.api.response.EventoAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.api.response.PaginaAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.AuditoriaConsultaJdbcRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaConsultaServiceTest {

    @Mock
    private AuditoriaConsultaJdbcRepository repository;

    @InjectMocks
    private AuditoriaConsultaService service;

    private UUID idEvento;
    private UUID idUsuario;
    private UUID idEntidad;
    private OffsetDateTime fechaEvento;
    private EventoAuditoria evento;

    @BeforeEach
    void setUp() {
        idEvento = UUID.randomUUID();
        idUsuario = UUID.randomUUID();
        idEntidad = UUID.randomUUID();
        fechaEvento = OffsetDateTime.parse("2026-11-20T10:15:30-05:00");
        evento = new EventoAuditoria(
                idEvento,
                idUsuario,
                fechaEvento,
                "PUBLICACION_APROBADA",
                "EXITOSO",
                "PUBLICACION",
                idEntidad,
                "{\"estado\":\"ACTIVA\"}",
                "correlacion-123");
    }

    @Test
    void consultaFiltradaDevuelvePaginaYNormalizaLosFiltros() {
        OffsetDateTime desde = fechaEvento.minusDays(1);
        OffsetDateTime hasta = fechaEvento.plusDays(1);
        when(repository.contar(any(FiltrosAuditoria.class))).thenReturn(21L);
        when(repository.consultar(any(FiltrosAuditoria.class), org.mockito.ArgumentMatchers.eq(10),
                org.mockito.ArgumentMatchers.eq(20L))).thenReturn(List.of(evento));

        PaginaAuditoriaResponse respuesta = service.consultar(
                idUsuario, desde, hasta, " PUBLICACION_APROBADA ", " EXITOSO ", " PUBLICACION ", 2, 10);

        assertEquals(List.of(new EventoAuditoriaResponse(
                idEvento, idUsuario, fechaEvento, "PUBLICACION_APROBADA", "EXITOSO",
                "PUBLICACION", idEntidad, "{\"estado\":\"ACTIVA\"}", "correlacion-123")),
                respuesta.eventos());
        assertEquals(2, respuesta.pagina());
        assertEquals(10, respuesta.tamano());
        assertEquals(21L, respuesta.totalResultados());
        assertEquals(3, respuesta.totalPaginas());
        assertNull(respuesta.mensaje());

        ArgumentCaptor<FiltrosAuditoria> filtros = ArgumentCaptor.forClass(FiltrosAuditoria.class);
        verify(repository).consultar(filtros.capture(), org.mockito.ArgumentMatchers.eq(10),
                org.mockito.ArgumentMatchers.eq(20L));
        assertEquals(idUsuario, filtros.getValue().idUsuario());
        assertEquals(desde, filtros.getValue().desde());
        assertEquals(hasta, filtros.getValue().hasta());
        assertEquals("PUBLICACION_APROBADA", filtros.getValue().accion());
        assertEquals("EXITOSO", filtros.getValue().resultado());
        assertEquals("PUBLICACION", filtros.getValue().entidadAfectada());
    }

    @Test
    void consultaSinResultadosDevuelveListaVaciaYMensaje() {
        when(repository.contar(any(FiltrosAuditoria.class))).thenReturn(0L);
        when(repository.consultar(any(FiltrosAuditoria.class), org.mockito.ArgumentMatchers.eq(20),
                org.mockito.ArgumentMatchers.eq(0L))).thenReturn(List.of());

        PaginaAuditoriaResponse respuesta = service.consultar(
                null, null, null, null, null, null, 0, 20);

        assertEquals(List.of(), respuesta.eventos());
        assertEquals(0L, respuesta.totalResultados());
        assertEquals(0, respuesta.totalPaginas());
        assertEquals("No hay eventos de auditoría para los filtros indicados.", respuesta.mensaje());
    }

    @Test
    void consultaConDesdePosteriorAHastaEsRechazada() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.consultar(null, fechaEvento, fechaEvento.minusDays(1), null, null, null, 0, 20));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void consultaConPaginaNegativaEsRechazada() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.consultar(null, null, null, null, null, null, -1, 20));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void consultaConTamanoFueraDelLimiteEsRechazada() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.consultar(null, null, null, null, null, null, 0, 101));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void consultaDevuelve503SiElAlmacenNoEstaDisponible() {
        when(repository.contar(any(FiltrosAuditoria.class)))
                .thenThrow(new DataAccessResourceFailureException("almacén no disponible"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.consultar(null, null, null, null, null, null, 0, 20));

        assertEquals(503, error.getStatusCode().value());
    }

    @Test
    void consultaPorIdDevuelveElEventoEncontrado() {
        when(repository.buscar(idEvento)).thenReturn(Optional.of(evento));

        EventoAuditoriaResponse respuesta = service.consultar(idEvento);

        assertEquals(idEvento, respuesta.idEvento());
        assertEquals(idUsuario, respuesta.idUsuario());
        assertEquals(fechaEvento, respuesta.fechaEvento());
        assertEquals("PUBLICACION_APROBADA", respuesta.accion());
        assertEquals("EXITOSO", respuesta.resultado());
        assertEquals("PUBLICACION", respuesta.entidadAfectada());
        assertEquals(idEntidad, respuesta.idEntidadAfectada());
        assertEquals("{\"estado\":\"ACTIVA\"}", respuesta.detalleJson());
        assertEquals("correlacion-123", respuesta.idCorrelacion());
    }

    @Test
    void consultaPorIdInexistenteDevuelve404() {
        when(repository.buscar(idEvento)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.consultar(idEvento));

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void consultaPorIdNuloDevuelve400() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.consultar((UUID) null));

        assertEquals(400, error.getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void consultaPorIdDevuelve503SiElAlmacenNoEstaDisponible() {
        when(repository.buscar(idEvento))
                .thenThrow(new DataAccessResourceFailureException("almacén no disponible"));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.consultar(idEvento));

        assertEquals(503, error.getStatusCode().value());
    }
}