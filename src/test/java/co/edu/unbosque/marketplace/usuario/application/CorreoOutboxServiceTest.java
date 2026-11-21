package co.edu.unbosque.marketplace.usuario.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.usuario.application.port.CorreoPendienteRepository;
import co.edu.unbosque.marketplace.usuario.domain.CorreoPendiente;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento;

@ExtendWith(MockitoExtension.class)
class CorreoOutboxServiceTest {
    @Mock private CorreoPendienteRepository repository;
    @Mock private ProteccionDocumento proteccion;
    @InjectMocks private CorreoOutboxService service;

    @Test
    void encolarCodigoGuardaContenidoCifradoYEstadoPendiente() {
        when(proteccion.cifrarTexto("123456"))
                .thenReturn(new ProteccionDocumento.TextoCifrado(new byte[]{1,2}, new byte[]{3}));
        service.encolarCodigoVerificacion("ana@example.com", "123456");
        ArgumentCaptor<CorreoPendiente> captor = ArgumentCaptor.forClass(CorreoPendiente.class);
        verify(repository).save(captor.capture());
        CorreoPendiente guardado = captor.getValue();
        assertAll(
                () -> assertEquals("ana@example.com", guardado.getDestinatario()),
                () -> assertEquals("VERIFICACION", guardado.getTipo()),
                () -> assertEquals("PENDIENTE", guardado.getEstado()),
                () -> assertEquals(0, guardado.getIntentos()),
                () -> assertNotNull(guardado.getClaveIdempotencia()),
                () -> assertArrayEquals(new byte[]{1,2}, guardado.getContenidoCifrado())
        );
    }

    @Test
    void reclamarMarcaEnProcesoYDevuelveCodigoDescifrado() {
        CorreoPendiente pendiente = new CorreoPendiente();
        pendiente.setIdCorreo(UUID.randomUUID());
        pendiente.setDestinatario("ana@example.com");
        pendiente.setTipo("VERIFICACION");
        pendiente.setContenidoCifrado(new byte[]{1});
        pendiente.setNonce(new byte[]{2});
        pendiente.setClaveIdempotencia("idempotency");
        pendiente.setEstado("PENDIENTE");
        when(repository.reclamar(any(OffsetDateTime.class), any())).thenReturn(List.of(pendiente));
        when(proteccion.descifrarTexto(new byte[]{1}, new byte[]{2})).thenReturn("123456");
        var resultado = service.reclamarPendientes(10);
        assertEquals(1, resultado.size());
        assertEquals("123456", resultado.get(0).codigo());
        assertEquals("EN_PROCESO", pendiente.getEstado());
        assertEquals(1, pendiente.getIntentos());
        assertNotNull(pendiente.getLeaseHasta());
    }

    @Test
    void marcaErrorYProgramaReintento() {
        CorreoPendiente pendiente = new CorreoPendiente();
        pendiente.setIdCorreo(UUID.randomUUID());
        pendiente.setEstado("EN_PROCESO");
        pendiente.setIntentos(1);
        when(repository.findById(pendiente.getIdCorreo())).thenReturn(java.util.Optional.of(pendiente));
        service.marcarError(pendiente.getIdCorreo(), new RuntimeException("SMTP no disponible"));
        assertEquals("ERROR", pendiente.getEstado());
        assertEquals("RuntimeException: SMTP no disponible", pendiente.getUltimoError());
        assertNotNull(pendiente.getProximaEjecucion());
    }
}

