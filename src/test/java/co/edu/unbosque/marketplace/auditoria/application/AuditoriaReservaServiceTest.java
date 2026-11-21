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

import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoReserva;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.BandejaSalidaAuditoriaRepository;

@ExtendWith(MockitoExtension.class)
class AuditoriaReservaServiceTest {

    @Mock private BandejaSalidaAuditoriaRepository repository;
    @InjectMocks private AuditoriaReservaService service;

    @Test
    void registraEventoDeReservaEnLaOutbox() {
        UUID user = UUID.randomUUID();
        UUID reserva = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        service.registrarEvento(new RegistrarEventoReserva(user, reserva, fecha,
                "RESERVA_CANCELADA", "EXITOSO", "{}"));

        ArgumentCaptor<BandejaSalidaAuditoria> captor = ArgumentCaptor.forClass(BandejaSalidaAuditoria.class);
        verify(repository).save(captor.capture());
        BandejaSalidaAuditoria evento = captor.getValue();
        assertNotNull(evento.getIdEvento());
        assertEquals(user, evento.getIdUsuario());
        assertEquals(reserva, evento.getIdEntidadAfectada());
        assertEquals(fecha, evento.getFechaEvento());
        assertEquals("RESERVA", evento.getEntidadAfectada());
        assertEquals("RESERVA_CANCELADA", evento.getAccion());
        assertEquals("EXITOSO", evento.getResultado());
        assertEquals("{}", evento.getDetalle());
        assertEquals("PENDIENTE", evento.getEstadoEnvio());
        assertEquals(0, evento.getIntentosEnvio());
        assertEquals(fecha, evento.getProximaEjecucion());
        assertNotNull(UUID.fromString(evento.getIdCorrelacion()));
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {
        OffsetDateTime fecha = OffsetDateTime.now();
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(null));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(
                new RegistrarEventoReserva(id, null, fecha, "ACCION", "EXITOSO", "{}")));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(
                new RegistrarEventoReserva(id, id, fecha, " ", "EXITOSO", "{}")));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvento(
                new RegistrarEventoReserva(id, id, fecha, "ACCION", "EXITOSO", " ")));
        verifyNoInteractions(repository);
    }
}