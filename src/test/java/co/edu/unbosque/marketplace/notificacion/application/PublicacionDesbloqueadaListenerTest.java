package co.edu.unbosque.marketplace.notificacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDesbloqueadaEvent;

@ExtendWith(MockitoExtension.class)
class PublicacionDesbloqueadaListenerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private PublicacionDesbloqueadaListener listener;

    private PublicacionDesbloqueadaEvent evento;

    @BeforeEach
    void setUp() {
        evento = new PublicacionDesbloqueadaEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Apartamento moderno",
            "Se corrigió la información requerida.",
            OffsetDateTime.parse("2026-11-21T10:00:00-05:00")
        );
    }

    @Test
    void alDesbloquearPublicacionDebeRegistrarNotificacion() {
        listener.alDesbloquearPublicacion(evento);

        verify(notificacionService, times(1))
            .registrarPublicacionDesbloqueada(evento);
    }

    @Test
    void alDesbloquearPublicacionDebeEnviarElMismoEvento() {
        listener.alDesbloquearPublicacion(evento);

        verify(notificacionService)
            .registrarPublicacionDesbloqueada(same(evento));
    }

    @Test
    void alDesbloquearPublicacionNoDebeInvocarOtrosMetodos() {
        listener.alDesbloquearPublicacion(evento);

        verifyNoMoreInteractions(notificacionService);
    }

    @Test
    void debeTenerConfiguradaLaFaseBeforeCommit() throws NoSuchMethodException {
        TransactionalEventListener anotacion =
            PublicacionDesbloqueadaListener.class
                .getMethod(
                    "alDesbloquearPublicacion",
                    PublicacionDesbloqueadaEvent.class
                )
                .getAnnotation(TransactionalEventListener.class);

        assertNotNull(anotacion);
        assertEquals(TransactionPhase.BEFORE_COMMIT, anotacion.phase());
    }
}
