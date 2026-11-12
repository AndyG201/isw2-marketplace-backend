package co.edu.unbosque.marketplace.notificacion.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.unbosque.marketplace.publicacion.contracts.ModoAceptacionActualizadoEvent;

@Component
public class ModoAceptacionActualizadoListener {

    private final NotificacionService notificacionService;

    public ModoAceptacionActualizadoListener(
            NotificacionService notificacionService) {

        this.notificacionService = notificacionService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void alActualizarModoAceptacion(
            ModoAceptacionActualizadoEvent evento) {

        notificacionService.registrarModoAceptacionActualizado(evento);
    }
}
