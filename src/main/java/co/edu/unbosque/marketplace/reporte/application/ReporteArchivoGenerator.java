package co.edu.unbosque.marketplace.reporte.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.reporte.application.dto.DashboardIndicadoresResponse;

/** Genera el archivo del reporte de gestión (PDF o Excel) con los indicadores del momento. */
@Component
public class ReporteArchivoGenerator {

    private final DashboardIndicadoresService dashboard;
    private final Path directorio;

    public ReporteArchivoGenerator(
            DashboardIndicadoresService dashboard,
            @Value("${app.reportes.directorio:reportes}") String directorio) {
        this.dashboard = dashboard;
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public ReporteArchivoGenerado generar(UUID idExportacion, String formato)
            throws IOException {

        Files.createDirectories(directorio);

        DashboardIndicadoresResponse datos = dashboard.consultar();
        OffsetDateTime corte = datos.fechaCorteDatos();

        boolean pdf = formato.equals("PDF");
        Path archivo = directorio.resolve(idExportacion + (pdf ? ".pdf" : ".xlsx"));

        if (pdf) {
            ReportePdf.generar(archivo, datos);
        } else {
            ReporteExcel.generar(archivo, datos);
        }

        return new ReporteArchivoGenerado(archivo, corte);
    }
}
