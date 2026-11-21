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

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.auditoria.application.port.BandejaSalidaAuditoriaRepository;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoPublicacion;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

@ExtendWith(MockitoExtension.class)
class AuditoriaPublicacionServiceTest {

    @Mock private BandejaSalidaAuditoriaRepository repository;
    @InjectMocks private AuditoriaPublicacionService service;

    @Test
    void registraEventoDePublicacionEnLaOutbox() {
        UUID user = UUID.randomUUID();
        UUID publicacion = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        service.registrarEvento(new RegistrarEventoPublicacion(user, publicacion, fecha,
                "PUBLICACION_APROBADA", "EXITOSO", "{}"));

        ArgumentCaptor<BandejaSalidaAuditoria> captor = ArgumentCaptor.forClass(BandejaSalidaAuditoria.class);
        verify(repository).save(captor.capture());
        BandejaSalidaAuditoria evento = captor.getValue();
        assertNotNull(evento.getIdEvento());
        assertEquals(user, evento.getIdUsuario());
        assertEquals(publicacion, evento.getIdEntidadAfectada());
        assertEquals(fecha, evento.getFechaEvento());
        assertEquals("PUBLICACION", evento.getEntidadAfectada());
        assertEquals("PUBLICACION_APROBADA", evento.getAccion());
        assertEquals("EXITOSO", evento.getResultado());
        assertEquals("{}", evento.getDetalle());
        assertEquals("PENDIENTE", evento.getEstadoEnvio());
        assertEquals(0, evento.getIntentosEnvio());
        assertEquals(fecha, evento.getProximaEjecucion());
        assertNotNull(UUID.fromString(evento.getIdCorrelacion()));
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {
        UUID id = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.now();
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(null));
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(
                new RegistrarEventoPublicacion(id, null, fecha, "ACCION", "EXITOSO", "{}")));
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(
                new RegistrarEventoPublicacion(id, id, null, "ACCION", "EXITOSO", "{}")));
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(
                new RegistrarEventoPublicacion(id, id, fecha, " ", "EXITOSO", "{}")));
        verifyNoInteractions(repository);
    }
}