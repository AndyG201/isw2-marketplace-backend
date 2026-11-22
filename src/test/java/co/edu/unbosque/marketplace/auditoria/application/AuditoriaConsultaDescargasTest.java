package co.edu.unbosque.marketplace.auditoria.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import co.edu.unbosque.marketplace.auditoria.application.dto.PaginaAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.application.port.AuditoriaConsultaRepository;
import co.edu.unbosque.marketplace.auditoria.application.port.CacheEventosAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.AlmacenamientoInmutableAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;
import co.edu.unbosque.marketplace.compartido.domain.ServicioNoDisponibleException;

/** La página de auditoría descarga los eventos a la vez, en orden, y reutiliza los que ya están en la caché. */
class AuditoriaConsultaDescargasTest {

    private static final int EVENTOS = 6;

    private AuditoriaConsultaRepository repository;
    private AlmacenamientoInmutableAuditoria almacen;
    private CacheEventosAuditoria cache;
    private AuditoriaConsultaService servicio;
    private List<EventoAuditoriaIndice> indices;
    private Map<UUID, EventoAuditoria> eventos;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void preparar() {
        repository = mock(AuditoriaConsultaRepository.class);
        almacen = mock(AlmacenamientoInmutableAuditoria.class);
        cache = mock(CacheEventosAuditoria.class);
        ObjectProvider<AlmacenamientoInmutableAuditoria> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(almacen);
        when(cache.leer(any(), any())).thenReturn(Optional.empty());
        servicio = new AuditoriaConsultaService(repository, proveedor, cache);

        indices = new ArrayList<>();
        eventos = new HashMap<>();
        for (int i = 0; i < EVENTOS; i++) {
            UUID id = UUID.randomUUID();
            OffsetDateTime fecha = OffsetDateTime.parse("2026-11-20T10:00:00-05:00").minusMinutes(i);
            indices.add(new EventoAuditoriaIndice(id, null, fecha, "ACCION_" + i, "EXITOSO", "RESERVA", null, null,
                    "eventos/" + id + ".json", String.valueOf(i).repeat(64), "v" + i));
            eventos.put(id, new EventoAuditoria(id, null, fecha, "ACCION_" + i, "EXITOSO", "RESERVA", null, null, null));
        }
        when(repository.contar(any(FiltrosAuditoria.class))).thenReturn((long) EVENTOS);
        when(repository.consultar(any(FiltrosAuditoria.class), eq(20), eq(0L))).thenReturn(indices);
    }

    private PaginaAuditoriaResponse consultar() {
        return servicio.consultar(null, null, null, null, null, null, 0, 20);
    }

    @Test
    void descargaLosEventosAlMismoTiempoYConservaElOrdenDelIndice() throws Exception {
        // Cada descarga espera a que lleguen todas: solo termina si las 6 corren a la vez.
        CountDownLatch todasEnCurso = new CountDownLatch(EVENTOS);
        AtomicInteger simultaneas = new AtomicInteger();
        AtomicInteger maximo = new AtomicInteger();
        when(almacen.obtenerEvento(anyString(), anyString(), anyString(), any(UUID.class))).thenAnswer(llamada -> {
            maximo.accumulateAndGet(simultaneas.incrementAndGet(), Math::max);
            todasEnCurso.countDown();
            assertTrue(todasEnCurso.await(5, TimeUnit.SECONDS), "las descargas no corrieron a la vez");
            simultaneas.decrementAndGet();
            return eventos.get(llamada.<UUID>getArgument(3));
        });

        PaginaAuditoriaResponse pagina = consultar();

        assertEquals(EVENTOS, pagina.eventos().size());
        for (int i = 0; i < EVENTOS; i++) {
            assertEquals(indices.get(i).idEvento(), pagina.eventos().get(i).idEvento(), "posición " + i);
        }
        assertEquals(EVENTOS, maximo.get());
    }

    @Test
    void cadaEventoDescargadoSeGuardaEnLaCache() {
        when(almacen.obtenerEvento(anyString(), anyString(), anyString(), any(UUID.class)))
                .thenAnswer(llamada -> eventos.get(llamada.<UUID>getArgument(3)));

        consultar();

        for (EventoAuditoriaIndice indice : indices) {
            verify(cache).guardar(indice.sha256(), eventos.get(indice.idEvento()));
        }
    }

    @Test
    void losEventosQueYaEstanEnLaCacheNoSeDescargan() {
        for (EventoAuditoriaIndice indice : indices) {
            when(cache.leer(indice.idEvento(), indice.sha256())).thenReturn(Optional.of(eventos.get(indice.idEvento())));
        }

        PaginaAuditoriaResponse pagina = consultar();

        assertEquals(EVENTOS, pagina.eventos().size());
        verify(almacen, never()).obtenerEvento(any(), any(), any(), any());
        verify(cache, never()).guardar(any(), any());
    }

    @Test
    void conAlgunosEnCacheSoloDescargaLosQueFaltan() {
        EventoAuditoriaIndice enCache = indices.get(2);
        when(cache.leer(enCache.idEvento(), enCache.sha256())).thenReturn(Optional.of(eventos.get(enCache.idEvento())));
        when(almacen.obtenerEvento(anyString(), anyString(), anyString(), any(UUID.class)))
                .thenAnswer(llamada -> eventos.get(llamada.<UUID>getArgument(3)));

        PaginaAuditoriaResponse pagina = consultar();

        assertEquals(EVENTOS, pagina.eventos().size());
        verify(almacen, times(EVENTOS - 1)).obtenerEvento(any(), any(), any(), any());
        verify(almacen, never()).obtenerEvento(any(), any(), any(), eq(enCache.idEvento()));
    }

    @Test
    void siUnaDescargaFallaLaConsultaDevuelve503ComoAntes() {
        when(almacen.obtenerEvento(anyString(), anyString(), anyString(), any(UUID.class))).thenAnswer(llamada -> {
            UUID id = llamada.getArgument(3);
            if (id.equals(indices.get(3).idEvento())) {
                throw new IllegalStateException("La integridad del evento de auditoría no pudo verificarse.");
            }
            return eventos.get(id);
        });

        assertThrows(ServicioNoDisponibleException.class, this::consultar);
    }

    @Test
    void unaPaginaConUnSoloEventoSeDescargaSinHilosAdicionales() {
        when(repository.consultar(any(FiltrosAuditoria.class), eq(20), eq(0L))).thenReturn(List.of(indices.get(0)));
        when(almacen.obtenerEvento(anyString(), anyString(), anyString(), any(UUID.class)))
                .thenAnswer(llamada -> eventos.get(llamada.<UUID>getArgument(3)));

        PaginaAuditoriaResponse pagina = consultar();

        assertEquals(1, pagina.eventos().size());
        assertEquals(indices.get(0).idEvento(), pagina.eventos().get(0).idEvento());
    }

    @Test
    void unErrorNoControladoDeLaDescargaSePropagaTalCual() {
        when(almacen.obtenerEvento(anyString(), anyString(), anyString(), any(UUID.class)))
                .thenThrow(new IllegalArgumentException("Los datos de consulta del evento son obligatorios."));

        assertThrows(IllegalArgumentException.class, this::consultar);
    }

    @Test
    void siElHiloDeLaConsultaSeInterrumpeDevuelve503() {
        CountDownLatch bloqueada = new CountDownLatch(1);
        when(almacen.obtenerEvento(anyString(), anyString(), anyString(), any(UUID.class))).thenAnswer(llamada -> {
            bloqueada.await(5, TimeUnit.SECONDS);
            return eventos.get(llamada.<UUID>getArgument(3));
        });
        Thread.currentThread().interrupt();
        try {
            assertThrows(ServicioNoDisponibleException.class, this::consultar);
            assertTrue(Thread.currentThread().isInterrupted(), "debe conservar la marca de interrupción");
        } finally {
            Thread.interrupted();
            bloqueada.countDown();
        }
    }
}
