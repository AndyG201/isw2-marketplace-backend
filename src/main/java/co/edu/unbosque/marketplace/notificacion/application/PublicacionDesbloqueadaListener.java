
package co.edu.unbosque.marketplace.notificacion.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDesbloqueadaEvent;

@Component
public class PublicacionDesbloqueadaListener {

    private final NotificacionService notificacionService;

    public PublicacionDesbloqueadaListener(
            NotificacionService notificacionService) {

        this.notificacionService = notificacionService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void alDesbloquearPublicacion(
            PublicacionDesbloqueadaEvent evento) {

        notificacionService.registrarPublicacionDesbloqueada(evento);
    }
}
