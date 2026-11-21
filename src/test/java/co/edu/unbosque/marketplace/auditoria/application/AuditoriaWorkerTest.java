package co.edu.unbosque.marketplace.auditoria.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@ExtendWith(MockitoExtension.class)
class AuditoriaWorkerTest {

    @Mock
    private AuditoriaOutboxService outbox;

    @Mock
    private AlmacenAuditoriaInmutableJdbc almacen;

    @InjectMocks
    private AuditoriaWorker worker;

    private UUID idEvento;
    private BandejaSalidaAuditoria evento;

    @BeforeEach
    void setUp() {
        idEvento = UUID.randomUUID();
        evento = new BandejaSalidaAuditoria();
        evento.setIdEvento(idEvento);
        evento.setEstadoEnvio("EN_PROCESO");
    }

    @Test
    void soportaSoloTrabajosDeCopiaDeAuditoria() {
        org.junit.jupiter.api.Assertions.assertTrue(worker.soporta(TipoTrabajo.COPIAR_AUDITORIA));
        org.junit.jupiter.api.Assertions.assertFalse(worker.soporta(TipoTrabajo.EXPORTAR_REPORTE));
    }

    @Test
    void rechazaTipoDeTrabajoNoSoportado() {
        assertThrows(IllegalArgumentException.class,
                () -> worker.procesar(TipoTrabajo.EXPORTAR_REPORTE, idEvento.toString()));
        verifyNoInteractions(outbox, almacen);
    }

    @Test
    void rechazaIdentificadorQueNoEsUuid() {
        assertThrows(IllegalArgumentException.class,
                () -> worker.procesar(TipoTrabajo.COPIAR_AUDITORIA, "id-invalido"));
        verifyNoInteractions(outbox, almacen);
    }

    @Test
    void noHaceNadaSiElEventoNoExiste() {
        when(outbox.buscarParaProcesar(idEvento)).thenReturn(null);

        worker.procesar(TipoTrabajo.COPIAR_AUDITORIA, idEvento.toString());

        verify(almacen, never()).anexar(evento);
        verify(outbox, never()).marcarEnviado(idEvento);
    }

    @Test
    void noVuelveAEnviarEventoYaEnviado() {
        evento.setEstadoEnvio("ENVIADO");
        when(outbox.buscarParaProcesar(idEvento)).thenReturn(evento);

        worker.procesar(TipoTrabajo.COPIAR_AUDITORIA, idEvento.toString());

        verify(almacen, never()).anexar(evento);
        verify(outbox, never()).marcarEnviado(idEvento);
    }

    @Test
    void copiaEventoYLoMarcaEnviado() {
        when(outbox.buscarParaProcesar(idEvento)).thenReturn(evento);

        worker.procesar(TipoTrabajo.COPIAR_AUDITORIA, idEvento.toString());

        verify(almacen).anexar(evento);
        verify(outbox).marcarEnviado(idEvento);
    }

    @Test
    void registraErrorSiFallaElAlmacen() {
        IllegalStateException fallo = new IllegalStateException("almacén no disponible");
        when(outbox.buscarParaProcesar(idEvento)).thenReturn(evento);
        org.mockito.Mockito.doThrow(fallo).when(almacen).anexar(evento);

        worker.procesar(TipoTrabajo.COPIAR_AUDITORIA, idEvento.toString());

        verify(outbox).marcarError(idEvento, fallo);
        verify(outbox, never()).marcarEnviado(idEvento);
    }
}