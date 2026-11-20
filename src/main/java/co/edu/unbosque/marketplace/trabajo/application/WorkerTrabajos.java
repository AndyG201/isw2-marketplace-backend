package co.edu.unbosque.marketplace.trabajo.application;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.reporte.application.ExportacionReporteWorker;
import co.edu.unbosque.marketplace.reserva.application.ReservaAprobacionRetencionScheduler;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoConciliacionWorker;
import co.edu.unbosque.marketplace.reserva.application.ReservaRetencionScheduler;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;

@Component
@Profile("worker")
public class WorkerTrabajos {

    private final ColaTrabajosRedis cola;
    private final ReservaPagoConciliacionWorker conciliador;
    private final ReservaAprobacionRetencionScheduler aprobacion;
    private final ReservaRetencionScheduler retenciones;
    private final ExportacionReporteWorker exportaciones;

    public WorkerTrabajos(
            ColaTrabajosRedis cola,
            ReservaPagoConciliacionWorker conciliador,
            ReservaAprobacionRetencionScheduler aprobacion,
            ReservaRetencionScheduler retenciones,
            ExportacionReporteWorker exportaciones) {
        this.cola = cola;
        this.conciliador = conciliador;
        this.aprobacion = aprobacion;
        this.retenciones = retenciones;
        this.exportaciones = exportaciones;
    }

    @Scheduled(fixedDelayString = "${app.worker.intervalo-ms:250}")
    public void procesarSiguiente() {
        cola.tomarSiguiente().ifPresent(trabajo -> {
            switch (trabajo.tipo()) {
                case CONCILIAR_PAGOS ->
                        conciliador.conciliarPendientes();
                case LIBERAR_RETENCIONES_APROBACION ->
                        aprobacion.rechazarSolicitudesVencidas();
                case EXPIRAR_RETENCIONES ->
                        retenciones.expirarReservasSinPago();
                case EXPORTAR_REPORTE ->
                        exportaciones.procesar(
                                UUID.fromString(trabajo.identificador()));
            }
        });
    }
}