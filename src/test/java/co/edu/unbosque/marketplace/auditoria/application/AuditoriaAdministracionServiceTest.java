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
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAdministracion;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

@ExtendWith(MockitoExtension.class)
class AuditoriaAdministracionServiceTest {

    @Mock private BandejaSalidaAuditoriaRepository repository;
    @InjectMocks private AuditoriaAdministracionService service;

    @Test
    void registraEventoAdministrativoConCorrelacionProporcionada() {
        UUID user = UUID.randomUUID();
        UUID entity = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-20T10:00:00Z");
        service.registrarEvento(new RegistrarEventoAdministracion(user, entity, "USUARIO", fecha,
                "CUENTA_RESTRINGIDA", "EXITOSO", "{}", "corr-42"));

        ArgumentCaptor<BandejaSalidaAuditoria> captor = ArgumentCaptor.forClass(BandejaSalidaAuditoria.class);
        verify(repository).save(captor.capture());
        BandejaSalidaAuditoria evento = captor.getValue();
        assertNotNull(evento.getIdEvento());
        assertEquals(user, evento.getIdUsuario());
        assertEquals(entity, evento.getIdEntidadAfectada());
        assertEquals(fecha, evento.getFechaEvento());
        assertEquals("USUARIO", evento.getEntidadAfectada());
        assertEquals("CUENTA_RESTRINGIDA", evento.getAccion());
        assertEquals("EXITOSO", evento.getResultado());
        assertEquals("{}", evento.getDetalle());
        assertEquals("corr-42", evento.getIdCorrelacion());
        assertEquals("PENDIENTE", evento.getEstadoEnvio());
        assertEquals(0, evento.getIntentosEnvio());
        assertEquals(fecha, evento.getProximaEjecucion());
    }

    @Test
    void generaCorrelacionCuandoNoSeProporciona() {
        UUID entity = UUID.randomUUID();
        service.registrarEvento(new RegistrarEventoAdministracion(null, entity, "USUARIO",
                OffsetDateTime.now(), "ACCION", "EXITOSO", null, null));

        ArgumentCaptor<BandejaSalidaAuditoria> captor = ArgumentCaptor.forClass(BandejaSalidaAuditoria.class);
        verify(repository).save(captor.capture());
        assertNotNull(UUID.fromString(captor.getValue().getIdCorrelacion()));
    }

    @Test
    void rechazaCamposObligatoriosAusentes() {
        OffsetDateTime fecha = OffsetDateTime.now();
        UUID id = UUID.randomUUID();
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(null));
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(
                new RegistrarEventoAdministracion(null, id, " ", fecha, "ACCION", "EXITOSO", null, null)));
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(
                new RegistrarEventoAdministracion(null, id, "USUARIO", null, "ACCION", "EXITOSO", null, null)));
        assertThrows(DatosInvalidosException.class, () -> service.registrarEvento(
                new RegistrarEventoAdministracion(null, id, "USUARIO", fecha, "ACCION", " ", null, null)));
        verifyNoInteractions(repository);
    }
}