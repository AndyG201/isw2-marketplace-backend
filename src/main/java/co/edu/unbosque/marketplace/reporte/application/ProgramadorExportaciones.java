package co.edu.unbosque.marketplace.reporte.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.reporte.application.port.ExportacionReporteRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Component
@Profile("worker")
public class ProgramadorExportaciones {

    private final ColaTrabajosRedis cola;
    private final ExportacionReporteRepository exportaciones;

    public ProgramadorExportaciones(
            ColaTrabajosRedis cola,
            ExportacionReporteRepository exportaciones) {
        this.cola = cola;
        this.exportaciones = exportaciones;
    }

    @Scheduled(fixedDelayString = "${app.reportes.recuperacion-ms:15000}")
    public void recuperarExportacionesSolicitadas() {
        exportaciones.recuperarProcesosAbandonados(
                OffsetDateTime.now().minusMinutes(15));
        for (UUID id : exportaciones.listarIdsSolicitados(100)) {
            cola.encolar(TipoTrabajo.EXPORTAR_REPORTE, id.toString());
        }
    }
}