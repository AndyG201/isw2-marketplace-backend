package co.edu.unbosque.marketplace.reserva.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaCompletadaScheduler {

    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private final ReservaCompletadaService servicio;
    private final Clock reloj;

    @Autowired
    public ReservaCompletadaScheduler(ReservaCompletadaService servicio) {
        this(servicio, Clock.system(ZONA_COLOMBIA));
    }

    ReservaCompletadaScheduler(ReservaCompletadaService servicio, Clock reloj) {
        this.servicio = servicio;
        this.reloj = reloj;
    }

    @Scheduled(fixedDelayString = "${app.reserva.completada.revision-ms:3600000}")
    public void completarReservasFinalizadas() {
        servicio.completarReservasFinalizadas(LocalDate.now(reloj));
    }
}
