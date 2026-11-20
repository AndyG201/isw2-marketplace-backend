package co.edu.unbosque.marketplace.auditoria.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoResena;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.BandejaSalidaAuditoriaRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaResenaServiceTest {

    @Mock private BandejaSalidaAuditoriaRepository repository;
    @InjectMocks private AuditoriaResenaService service;

    @Test
    void registraEventoDeResenaEnLaOutbox() {
        UUID user = UUID.randomUUID();
        UUID resena = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        service.registrarEvento(new RegistrarEventoResena(user, resena, fecha,
                "RESENA_OCULTADA", "EXITOSO", "{}"));

        ArgumentCaptor<BandejaSalidaAuditoria> captor = ArgumentCaptor.forClass(BandejaSalidaAuditoria.class);
        verify(repository).save(captor.capture());
        BandejaSalidaAuditoria evento = captor.getValue();
        assertNotNull(evento.getIdEvento());
        assertEquals(user, evento.getIdUsuario());
        assertEquals(resena, evento.getIdEntidadAfectada());
        assertEquals(fecha, evento.getFechaEvento());
        assertEquals("RESENA", evento.getEntidadAfectada());
        assertEquals("RESENA_OCULTADA", evento.getAccion());
        assertEquals("EXITOSO", evento.getResultado());
        assertEquals("{}", evento.getDetalle());
        assertEquals("PENDIENTE", evento.getEstadoEnvio());
        assertEquals(0, evento.getIntentosEnvio());
        assertEquals(fecha, evento.getProximaEjecucion());
        assertNotNull(UUID.fromString(evento.getIdCorrelacion()));
    }

    @Test
    void requiereUsuarioResenaFechaAccionResultadoYDetalle() {
        UUID id = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(null));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(
                new RegistrarEventoResena(null, id, fecha, "ACCION", "EXITOSO", "{}")));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(
                new RegistrarEventoResena(id, null, fecha, "ACCION", "EXITOSO", "{}")));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(
                new RegistrarEventoResena(id, id, fecha, "ACCION", "EXITOSO", " ")));
        verifyNoInteractions(repository);
    }
}