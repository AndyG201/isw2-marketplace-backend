package co.edu.unbosque.marketplace.publicacion.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.unbosque.marketplace.usuario.contracts.CuentaDesactivadaEvent;
import co.edu.unbosque.marketplace.usuario.contracts.CuentaRestringidaEvent;

@Component
public class PublicacionCuentaEventListener {

    private final PublicacionCuentaService publicacionCuentaService;

    public PublicacionCuentaEventListener(
            PublicacionCuentaService publicacionCuentaService) {

        this.publicacionCuentaService = publicacionCuentaService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void alDesactivarCuenta(CuentaDesactivadaEvent evento) {

        publicacionCuentaService.pausarPublicacionesActivas(
                evento.idUsuario(),
                evento.fechaEvento()
        );
    }

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void alRestringirCuenta(CuentaRestringidaEvent evento) {

        publicacionCuentaService.pausarPublicacionesActivas(
                evento.idUsuario(),
                evento.fechaEvento()
        );
    }
}
