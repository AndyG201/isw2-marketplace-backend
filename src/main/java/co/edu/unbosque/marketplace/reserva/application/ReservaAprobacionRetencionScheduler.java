package co.edu.unbosque.marketplace.reserva.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaAprobacionRetencionScheduler {
    private final ReservaRetencionService servicio;

    public ReservaAprobacionRetencionScheduler(ReservaRetencionService servicio) {
        this.servicio = servicio;
    }

    @Scheduled(fixedDelayString = "${app.reserva.retencion.aprobacion.revision-ms:60000}")
    public void rechazarSolicitudesVencidas() {
        servicio.rechazarRetencionesVencidas();
    }
}
