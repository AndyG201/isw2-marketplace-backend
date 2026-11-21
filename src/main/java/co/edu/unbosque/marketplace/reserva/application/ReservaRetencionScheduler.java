package co.edu.unbosque.marketplace.reserva.application;

import org.springframework.stereotype.Component;

@Component
public class ReservaRetencionScheduler {
    private final ReservaConfirmacionService servicio;

    public ReservaRetencionScheduler(ReservaConfirmacionService servicio) {
        this.servicio = servicio;
    }

    public void expirarReservasSinPago() {
        servicio.expirarRetenciones();
    }
}
