package co.edu.unbosque.marketplace.reporte.application;

import java.nio.file.Path;
import java.time.OffsetDateTime;

public record ReporteArchivoGenerado(
        Path ruta,
        OffsetDateTime fechaCorte) {
}