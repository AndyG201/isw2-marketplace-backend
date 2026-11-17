package co.edu.unbosque.marketplace.reserva.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaRetencionScheduler {
    private final ReservaConfirmacionService servicio;

    public ReservaRetencionScheduler(ReservaConfirmacionService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(fixedDelayString = "${app.reserva.retencion.revision-ms:60000}")
    public void expirarReservasSinPago() {
        servicio.expirarRetenciones();
    }
}
