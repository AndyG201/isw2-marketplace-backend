package co.edu.unbosque.marketplace.auditoria.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.AuditoriaIndiceJdbcRepository;
import co.edu.unbosque.marketplace.auditoria.infrastructure.storage.BackblazeAuditoriaService;
import co.edu.unbosque.marketplace.auditoria.infrastructure.storage.ResultadoAlmacenamientoAuditoria;

@ExtendWith(MockitoExtension.class)
class AlmacenAuditoriaInmutableJdbcTest {

    @Mock
    private BackblazeAuditoriaService backblaze;

    @Mock
    private AuditoriaIndiceJdbcRepository indiceRepository;

    @InjectMocks
    private AlmacenAuditoriaInmutableJdbc almacen;

    @Test
    void almacenaEnBackblazeYRegistraElIndice() {

        BandejaSalidaAuditoria evento = evento();

        ResultadoAlmacenamientoAuditoria resultado =
                new ResultadoAlmacenamientoAuditoria(
                        "eventos/2026/11/20/" + evento.getIdEvento() + ".json",
                        "a".repeat(64),
                        "COMPLIANCE",
                        OffsetDateTime.now().plusDays(365),
                        "version-123"
                );

        when(backblaze.almacenar(evento)).thenReturn(resultado);

        almacen.anexar(evento);

        verify(backblaze).almacenar(evento);
        verify(indiceRepository).registrar(evento, resultado);
    }

    @Test
    void noRegistraElIndiceSiFallaBackblaze() {

        BandejaSalidaAuditoria evento = evento();

        IllegalStateException error =
                new IllegalStateException("Backblaze no disponible");

        when(backblaze.almacenar(evento)).thenThrow(error);

        assertThrows(
                IllegalStateException.class,
                () -> almacen.anexar(evento)
        );

        verifyNoInteractions(indiceRepository);
    }

    @Test
    void rechazaEventoNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> almacen.anexar(null)
        );

        verifyNoInteractions(backblaze, indiceRepository);
    }

    private BandejaSalidaAuditoria evento() {

        BandejaSalidaAuditoria evento = new BandejaSalidaAuditoria();

        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(UUID.randomUUID());
        evento.setFechaEvento(OffsetDateTime.now());
        evento.setAccion("PUBLICACION_APROBADA");
        evento.setResultado("EXITOSO");
        evento.setEntidadAfectada("PUBLICACION");
        evento.setIdEntidadAfectada(UUID.randomUUID());
        evento.setDetalle("{\"estado\":\"ACTIVA\"}");
        evento.setIdCorrelacion("corr-test");

        return evento;
    }
}
