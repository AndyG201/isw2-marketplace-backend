package co.edu.unbosque.marketplace.notificacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.unbosque.marketplace.publicacion.contracts.ModoAceptacionActualizadoEvent;

@ExtendWith(MockitoExtension.class)
class ModoAceptacionActualizadoListenerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private ModoAceptacionActualizadoListener listener;

    private ModoAceptacionActualizadoEvent evento;

    @BeforeEach
    void setUp() {
        evento = new ModoAceptacionActualizadoEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Apartamento moderno",
                "APROBACION",
                "INMEDIATA",
                OffsetDateTime.parse("2026-11-21T10:00:00-05:00")
        );
    }

    @Test
    void alActualizarModoAceptacionDebeRegistrarNotificacion() {

        listener.alActualizarModoAceptacion(evento);

        verify(notificacionService, times(1))
                .registrarModoAceptacionActualizado(evento);
    }

    @Test
    void alActualizarModoAceptacionDebeEnviarElMismoEvento() {

        listener.alActualizarModoAceptacion(evento);

        verify(notificacionService)
                .registrarModoAceptacionActualizado(same(evento));
    }

    @Test
    void alActualizarModoAceptacionNoDebeInvocarOtrosMetodos() {

        listener.alActualizarModoAceptacion(evento);

        verify(notificacionService)
                .registrarModoAceptacionActualizado(evento);

        verifyNoMoreInteractions(notificacionService);
    }

    @Test
    void debeTenerConfiguradaLaFaseBeforeCommit()
            throws NoSuchMethodException {

        TransactionalEventListener anotacion =
                ModoAceptacionActualizadoListener.class
                        .getMethod(
                                "alActualizarModoAceptacion",
                                ModoAceptacionActualizadoEvent.class
                        )
                        .getAnnotation(TransactionalEventListener.class);

        assertEquals(TransactionPhase.BEFORE_COMMIT, anotacion.phase());
    }
}
