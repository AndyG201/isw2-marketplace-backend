package co.edu.unbosque.marketplace.trabajo.application;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Component
@Profile("worker")
public class ProgramadorTrabajos {

    private final ColaTrabajosRedis cola;

    public ProgramadorTrabajos(ColaTrabajosRedis cola) {
        this.cola = cola;
    }

    @Scheduled(fixedDelayString = "${app.pago.conciliacion.intervalo-ms:30000}")
    public void programarConciliacionPagos() {
        cola.encolar(TipoTrabajo.CONCILIAR_PAGOS, "pendientes");
    }

    @Scheduled(fixedDelayString =
            "${app.reserva.retencion.aprobacion.revision-ms:60000}")
    public void programarRechazoDeRetenciones() {
        cola.encolar(TipoTrabajo.LIBERAR_RETENCIONES_APROBACION, "pendientes");
    }

    @Scheduled(fixedDelayString = "${app.reserva.retencion.revision-ms:60000}")
    public void programarExpiracionDeReservas() {
        cola.encolar(TipoTrabajo.EXPIRAR_RETENCIONES, "pendientes");
    }
}