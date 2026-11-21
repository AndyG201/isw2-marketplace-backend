package co.edu.unbosque.marketplace.usuario.application;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.usuario.domain.ServicioCorreo;

@ExtendWith(MockitoExtension.class)
class CorreoOutboxWorkerTest {
    @Mock private CorreoOutboxService outbox;
    @Mock private ServicioCorreo servicioCorreo;

    @Test
    void enviaYMarcaComoEnviado() {
        UUID id = UUID.randomUUID();
        when(outbox.reclamarPendientes(25)).thenReturn(List.of(
                new CorreoOutboxService.CorreoParaEnviar(id, "ana@example.com", "VERIFICACION", "123456", "key")
        ));
        new CorreoOutboxWorker(outbox, servicioCorreo).procesarPendientes();
        verify(servicioCorreo).enviarCodigoVerificacion("ana@example.com", "123456");
        verify(outbox).marcarEnviado(id);
        verify(outbox, never()).marcarError(any(), any());
    }

    @Test
    void unTipoDeCorreoNoSoportadoSeRegistraComoErrorSinEnviar() {
        UUID id = UUID.randomUUID();
        when(outbox.reclamarPendientes(25)).thenReturn(List.of(
                new CorreoOutboxService.CorreoParaEnviar(id, "ana@example.com", "OTRO_TIPO", "123456", "key")
        ));
        new CorreoOutboxWorker(outbox, servicioCorreo).procesarPendientes();
        verify(outbox).marcarError(eq(id), any(co.edu.unbosque.marketplace.compartido.domain.FallaInternaException.class));
        verifyNoInteractions(servicioCorreo);
        verify(outbox, never()).marcarEnviado(id);
    }

    @Test
    void fallaEnvioYRegistraErrorParaReintento() {
        UUID id = UUID.randomUUID();
        RuntimeException error = new RuntimeException("SMTP down");
        when(outbox.reclamarPendientes(25)).thenReturn(List.of(
                new CorreoOutboxService.CorreoParaEnviar(id, "ana@example.com", "VERIFICACION", "123456", "key")
        ));
        doThrow(error).when(servicioCorreo).enviarCodigoVerificacion("ana@example.com", "123456");
        new CorreoOutboxWorker(outbox, servicioCorreo).procesarPendientes();
        verify(outbox).marcarError(id, error);
        verify(outbox, never()).marcarEnviado(id);
    }
}
