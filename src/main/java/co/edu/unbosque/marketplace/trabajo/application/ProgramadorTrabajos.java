package co.edu.unbosque.marketplace.trabajo.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.reporte.infrastructure.persistence.ExportacionReporteJdbcRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Component
@Profile("worker")
public class ProgramadorTrabajos {

    private final ColaTrabajosRedis cola;
    private final ExportacionReporteJdbcRepository exportaciones;

    public ProgramadorTrabajos(
            ColaTrabajosRedis cola,
            ExportacionReporteJdbcRepository exportaciones) {
        this.cola = cola;
        this.exportaciones = exportaciones;
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

    @Scheduled(fixedDelayString = "${app.reportes.recuperacion-ms:15000}")
    public void recuperarExportacionesSolicitadas() {
        exportaciones.recuperarProcesosAbandonados(OffsetDateTime.now().minusMinutes(15));
        for (UUID id : exportaciones.listarIdsSolicitados(100)) {
            cola.encolar(TipoTrabajo.EXPORTAR_REPORTE, id.toString());
        }
    }
}