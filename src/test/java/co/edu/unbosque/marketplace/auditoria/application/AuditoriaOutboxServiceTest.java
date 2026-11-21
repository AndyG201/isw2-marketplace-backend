package co.edu.unbosque.marketplace.auditoria.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.auditoria.application.port.BandejaSalidaAuditoriaRepository;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@ExtendWith(MockitoExtension.class)
class AuditoriaOutboxServiceTest {

    @Mock
    private BandejaSalidaAuditoriaRepository repository;

    @Mock
    private ColaTrabajos cola;

    @InjectMocks
    private AuditoriaOutboxService service;

    private UUID idUsuario;
    private UUID idEntidad;
    private OffsetDateTime fechaEvento;

    @BeforeEach
    void setUp() {
        idUsuario = UUID.randomUUID();
        idEntidad = UUID.randomUUID();
        fechaEvento = OffsetDateTime.parse("2026-11-20T10:15:30-05:00");
    }

    @Test
    void registraEventoNormalizadoConEstadoPendiente() {
        service.registrarEvento(solicitud("  CAMBIAR_ESTADO  ", " EXITOSO ", " RESERVA ", " correlacion-1 "));

        ArgumentCaptor<BandejaSalidaAuditoria> captor = ArgumentCaptor.forClass(BandejaSalidaAuditoria.class);
        verify(repository).save(captor.capture());
        BandejaSalidaAuditoria evento = captor.getValue();
        assertNotNull(evento.getIdEvento());
        assertEquals(idUsuario, evento.getIdUsuario());
        assertEquals(fechaEvento, evento.getFechaEvento());
        assertEquals("CAMBIAR_ESTADO", evento.getAccion());
        assertEquals("EXITOSO", evento.getResultado());
        assertEquals("RESERVA", evento.getEntidadAfectada());
        assertEquals(idEntidad, evento.getIdEntidadAfectada());
        assertEquals("{\"estado\":\"CONFIRMADA\"}", evento.getDetalle());
        assertEquals("correlacion-1", evento.getIdCorrelacion());
        assertEquals("PENDIENTE", evento.getEstadoEnvio());
        assertEquals(0, evento.getIntentosEnvio());
        assertNull(evento.getFechaEnvio());
        assertNull(evento.getUltimoError());
        assertNotNull(evento.getProximaEjecucion());
    }

    @Test
    void generaCorrelacionSiNoSeProporciona() {
        service.registrarEvento(solicitud("ACCION", "EXITOSO", "RESERVA", " "));

        ArgumentCaptor<BandejaSalidaAuditoria> captor = ArgumentCaptor.forClass(BandejaSalidaAuditoria.class);
        verify(repository).save(captor.capture());
        assertDoesNotThrowUuid(captor.getValue().getIdCorrelacion());
    }

    @Test
    void rechazaSolicitudInvalidaOSuperaLongitudes() {
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(null));
        assertThrows(DatosInvalidosException.class,
                () -> service.registrarEvento(solicitud(" ", "EXITOSO", "RESERVA", null)));
        assertThrows(DatosInvalidosException.class,
                () -> service.registrarEvento(solicitud("A".repeat(101), "EXITOSO", "RESERVA", null)));
        verifyNoInteractions(repository);
    }

    @Test
    void registrarEventoIndependienteGuardaEnLaOutbox() {
        service.registrarEventoIndependiente(solicitud("ACCION", "EXITOSO", "RESERVA", null));

        verify(repository).save(any(BandejaSalidaAuditoria.class));
    }

    @Test
    void encolarPendientesSinEventosDevuelveCero() {
        when(repository.findTop100ByEstadoEnvioInAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(
                eq(List.of("PENDIENTE", "ERROR", "EN_PROCESO")), any(OffsetDateTime.class)))
                .thenReturn(List.of());

        assertEquals(0, service.encolarPendientes());
        verify(repository, never()).saveAll(anyList());
        verifyNoInteractions(cola);
    }

    @Test
    void encolarPendientesMarcaEventosYProgramaTrabajos() {
        UUID idEvento = UUID.randomUUID();
        BandejaSalidaAuditoria evento = evento(idEvento, "ERROR", 2);
        when(repository.findTop100ByEstadoEnvioInAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(
                eq(List.of("PENDIENTE", "ERROR", "EN_PROCESO")), any(OffsetDateTime.class)))
                .thenReturn(List.of(evento));

        OffsetDateTime inicio = OffsetDateTime.now(ZoneOffset.UTC);
        assertEquals(1, service.encolarPendientes());

        assertEquals("EN_PROCESO", evento.getEstadoEnvio());
        assertEquals(3, evento.getIntentosEnvio());
        assertTrue(!evento.getProximaEjecucion().isBefore(inicio.plusSeconds(59)));
        verify(repository).saveAll(List.of(evento));
        verify(cola).encolar(TipoTrabajo.COPIAR_AUDITORIA, idEvento.toString());
    }

    @Test
    void buscarParaProcesarRetornaNuloSiNoExiste() {
        UUID idEvento = UUID.randomUUID();
        when(repository.findById(idEvento)).thenReturn(Optional.empty());

        assertNull(service.buscarParaProcesar(idEvento));
    }

    @Test
    void marcarEnviadoActualizaEventoYLimpiaError() {
        UUID idEvento = UUID.randomUUID();
        BandejaSalidaAuditoria evento = evento(idEvento, "EN_PROCESO", 1);
        evento.setUltimoError("fallo anterior");
        when(repository.findById(idEvento)).thenReturn(Optional.of(evento));

        service.marcarEnviado(idEvento);

        assertEquals("ENVIADO", evento.getEstadoEnvio());
        assertNotNull(evento.getFechaEnvio());
        assertEquals(evento.getFechaEnvio(), evento.getProximaEjecucion());
        assertNull(evento.getUltimoError());
        verify(repository).save(evento);
    }

    @Test
    void marcarEnviadoNoGuardaSiYaEstabaEnviado() {
        UUID idEvento = UUID.randomUUID();
        BandejaSalidaAuditoria evento = evento(idEvento, "ENVIADO", 1);
        when(repository.findById(idEvento)).thenReturn(Optional.of(evento));

        service.marcarEnviado(idEvento);

        verify(repository, never()).save(any(BandejaSalidaAuditoria.class));
    }

    @Test
    void marcarErrorCalculaReintentoYGuardaMensaje() {
        UUID idEvento = UUID.randomUUID();
        BandejaSalidaAuditoria evento = evento(idEvento, "EN_PROCESO", 3);
        when(repository.findById(idEvento)).thenReturn(Optional.of(evento));
        OffsetDateTime inicio = OffsetDateTime.now(ZoneOffset.UTC);

        service.marcarError(idEvento, new IllegalStateException("almacén caído"));

        assertEquals("ERROR", evento.getEstadoEnvio());
        assertEquals("IllegalStateException: almacén caído", evento.getUltimoError());
        assertTrue(!evento.getProximaEjecucion().isBefore(inicio.plusSeconds(119)));
        assertTrue(evento.getProximaEjecucion().isBefore(inicio.plusSeconds(125)));
        verify(repository).save(evento);
    }

    @Test
    void marcarErrorLimitaElMensajeYNoModificaEventoEnviado() {
        UUID idError = UUID.randomUUID();
        BandejaSalidaAuditoria eventoError = evento(idError, "EN_PROCESO", 1);
        when(repository.findById(idError)).thenReturn(Optional.of(eventoError));
        service.marcarError(idError, new DatosInvalidosException("x".repeat(1100)));
        assertEquals(1000, eventoError.getUltimoError().length());

        UUID idEnviado = UUID.randomUUID();
        BandejaSalidaAuditoria eventoEnviado = evento(idEnviado, "ENVIADO", 1);
        when(repository.findById(idEnviado)).thenReturn(Optional.of(eventoEnviado));
        service.marcarError(idEnviado, new IllegalStateException("no debe cambiar"));
        verify(repository, never()).save(eventoEnviado);
        assertEquals("ENVIADO", eventoEnviado.getEstadoEnvio());
    }

    private RegistrarEventoAuditoria solicitud(String accion, String resultado, String entidad, String correlacion) {
        return new RegistrarEventoAuditoria(
                idUsuario, fechaEvento, accion, resultado, entidad, idEntidad,
                "{\"estado\":\"CONFIRMADA\"}", correlacion);
    }

    private BandejaSalidaAuditoria evento(UUID id, String estado, int intentos) {
        BandejaSalidaAuditoria evento = new BandejaSalidaAuditoria();
        evento.setIdEvento(id);
        evento.setEstadoEnvio(estado);
        evento.setIntentosEnvio(intentos);
        evento.setProximaEjecucion(OffsetDateTime.now(ZoneOffset.UTC));
        return evento;
    }

    private void assertDoesNotThrowUuid(String valor) {
        assertNotNull(valor);
        assertEquals(valor, UUID.fromString(valor).toString());
    }
}