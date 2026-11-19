package co.edu.unbosque.marketplace.usuario.application;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CuentasSinVerificarScheduler {

    private final CuentasSinVerificarService servicio;

    public CuentasSinVerificarScheduler(CuentasSinVerificarService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(
            fixedDelayString = "${app.usuario.pendiente.revision-ms:3600000}",
            initialDelayString = "${app.usuario.pendiente.revision-ms:3600000}")
    public void eliminarCuentasSinVerificar() {
        servicio.eliminarVencidas(OffsetDateTime.now());
    }
}
