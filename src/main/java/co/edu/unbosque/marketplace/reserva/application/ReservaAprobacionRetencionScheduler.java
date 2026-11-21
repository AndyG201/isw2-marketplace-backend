package co.edu.unbosque.marketplace.reserva.application;

import org.springframework.stereotype.Component;

@Component
public class ReservaAprobacionRetencionScheduler {
    private final ReservaRetencionService servicio;

    public ReservaAprobacionRetencionScheduler(ReservaRetencionService servicio) {
        this.servicio = servicio;
    }

    public void rechazarSolicitudesVencidas() {
        servicio.rechazarRetencionesVencidas();
    }
}
