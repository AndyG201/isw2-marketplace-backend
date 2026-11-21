package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentasSinVerificarSchedulerTest {

    @Mock private CuentasSinVerificarService servicio;
    @InjectMocks private CuentasSinVerificarScheduler scheduler;

    @Test
    void eliminaLasCuentasVencidasConLaHoraActual() {
        OffsetDateTime antes = OffsetDateTime.now();

        scheduler.eliminarCuentasSinVerificar();

        ArgumentCaptor<OffsetDateTime> ahora = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(servicio).eliminarVencidas(ahora.capture());
        assertFalse(ahora.getValue().isBefore(antes));
        assertFalse(ahora.getValue().isAfter(OffsetDateTime.now()));
    }
}
